package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.SessionDTO;
import com.bentork.ev_system.model.Receipt;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.repository.ReceiptRepository;
import com.bentork.ev_system.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private ReceiptService receiptService;

	@Autowired
	private WalletTransactionService walletTransactionService;

	@Autowired
	private AdminNotificationService adminNotificationService;

	@Autowired
	private RevenueService revenueService;

	@Autowired
	private UserNotificationService userNotificationService;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

	/**
	 * Start session only if receipt is already PAID.
	 * Auto-stop logic applies for both plan and selected kWh sessions.
	 */
	public Session startSessionFromReceipt(Receipt receipt, String boxId) {
		if (!"PAID".equalsIgnoreCase(receipt.getStatus())) {
			throw new RuntimeException("Cannot start session without a paid receipt.");
		}

		Session session = new Session();
		session.setUser(receipt.getUser());
		session.setCharger(receipt.getCharger());
		session.setBoxId(boxId);
		session.setStartTime(LocalDateTime.now());
		session.setStatus("active");
		session.setCreatedAt(LocalDateTime.now());
		sessionRepository.save(session);

		receipt.setSession(session);
		receiptRepository.save(receipt);

		// Auto-stop scheduling
		if (receipt.getPlan() != null) {
			// Stop after plan duration
			scheduleAutoStop(session.getId(), receipt.getPlan().getDurationMin());
		} else if (receipt.getSelectedKwh() != null) {
			// Approximation: minutes required = selectedKwh / 0.075
			double minutesRequired = receipt.getSelectedKwh().doubleValue() / 0.075;
			scheduleAutoStop(session.getId(), (int) Math.ceil(minutesRequired));
		}

		adminNotificationService.createSystemNotification(
				"User '" + session.getUser().getName() + "' started a session on charger '" +
						session.getCharger().getOcppId() + "'",
				"SESSION_STARTED");

		userNotificationService.createNotification(
				session.getUser().getId(),
				"Charging Started",
				"Your charging session has started successfully.",
				"INFO");

		return session;
	}

	/**
	 * Manual stop (by user).
	 */
	public Map<String, Object> stopSession(Long userId, SessionDTO request) {
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if (!session.getUser().getId().equals(userId)) {
			throw new RuntimeException("Unauthorized to stop this session");
		}

		if (!"active".equalsIgnoreCase(session.getStatus())) {
			return buildAlreadyCompletedResponse(session);
		}

		return finalizeSession(session, "MANUAL_STOP");
	}

	/**
	 * Auto-stop (triggered by scheduler).
	 */
	public void stopSessionBySystem(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if ("active".equalsIgnoreCase(session.getStatus())) {
			finalizeSession(session, "AUTO_STOP");
		}
	}

	/**
	 * Real-time check for selectedKwh session — stop when limit reached.
	 * Call this periodically when you get meter updates from charger.
	 */
	public void checkAndStopIfReachedKwh(Long sessionId, double currentKwh) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new RuntimeException("Session not found"));

		Receipt receipt = receiptRepository.findBySession(session).orElse(null);
		if (receipt != null && receipt.getSelectedKwh() != null &&
				"active".equalsIgnoreCase(session.getStatus())) {
			double targetKwh = receipt.getSelectedKwh().doubleValue();
			if (currentKwh >= targetKwh) {
				finalizeSession(session, "AUTO_STOP_KWH_REACHED");
			}
		}
	}

	/**
	 * Finalize session (shared logic).
	 */
	private Map<String, Object> finalizeSession(Session session, String stopReason) {
		if (!"active".equalsIgnoreCase(session.getStatus())) {
			return buildAlreadyCompletedResponse(session);
		}

		session.setEndTime(LocalDateTime.now());
		session.setStatus("completed");

		Receipt receipt = receiptRepository.findBySession(session).orElse(null);

		double energyUsed;
		BigDecimal finalCostBD;
		boolean refundIssued = false;
		boolean extraDebited = false;

		if (receipt != null && receipt.getSelectedKwh() != null) {
			// Prepaid selected kWh session → always use selectedKwh
			energyUsed = receipt.getSelectedKwh().doubleValue();
			finalCostBD = BigDecimal.valueOf(energyUsed)
					.multiply(BigDecimal.valueOf(session.getCharger().getRate()))
					.setScale(2, RoundingMode.HALF_UP);
		} else {
			// Plan session → calculate actual energy used
			energyUsed = calculateEnergyUsed(session);
			finalCostBD = BigDecimal.valueOf(energyUsed)
					.multiply(BigDecimal.valueOf(session.getCharger().getRate()))
					.setScale(2, RoundingMode.HALF_UP);

			if (receipt != null) {
				BigDecimal prepaid = receipt.getAmount();

				if (finalCostBD.compareTo(prepaid) < 0) {
					BigDecimal refund = prepaid.subtract(finalCostBD);
					walletTransactionService.credit(session.getUser().getId(), session.getId(),
							refund, "PLAN_SESSION_REFUND");
					refundIssued = true;
					userNotificationService.createNotification(
							session.getUser().getId(),
							"Refund Issued",
							"Unused amount ₹" + refund + " has been refunded to your wallet.",
							"REFUND");
				} else if (finalCostBD.compareTo(prepaid) > 0) {
					BigDecimal extra = finalCostBD.subtract(prepaid);
					walletTransactionService.debit(session.getUser().getId(), session.getId(),
							extra, "PLAN_SESSION_EXTRA_DEBIT");
					extraDebited = true;
					userNotificationService.createNotification(
							session.getUser().getId(),
							"Extra Debit",
							"Extra amount ₹" + extra + " has been deducted due to higher usage.",
							"DEBIT");
				}
			}
		}

		session.setEnergyKwh(energyUsed);
		session.setCost(finalCostBD.doubleValue());
		sessionRepository.save(session);

		if (receipt != null) {
			receiptService.finalizeReceipt(session, finalCostBD);
		}

		adminNotificationService.createSystemNotification(
				"User '" + session.getUser().getName() + "' stopped session. Energy used: " +
						String.format("%.2f", energyUsed) + " kWh, Final cost: ₹" + finalCostBD,
				"SESSION_COMPLETED");

		userNotificationService.createNotification(
				session.getUser().getId(),
				"Charging Stopped",
				"Your session has ended (" + stopReason + "). Total cost: ₹" + finalCostBD,
				"INFO");

		revenueService.recordRevenueForSession(session,
				finalCostBD.doubleValue(), "Wallet", null, "success");

		Map<String, Object> response = new HashMap<>();
		response.put("sessionId", session.getId());
		response.put("energyUsed", energyUsed);
		response.put("finalCost", finalCostBD);
		response.put("refundIssued", refundIssued);
		response.put("extraDebited", extraDebited);
		response.put("message", "Session completed (" + stopReason + ")" +
				(refundIssued ? " - Refund issued" : extraDebited ? " - Extra debited" : ""));
		return response;
	}

	private Map<String, Object> buildAlreadyCompletedResponse(Session session) {
		Map<String, Object> response = new HashMap<>();
		response.put("sessionId", session.getId());
		response.put("energyUsed", session.getEnergyKwh());
		response.put("finalCost", session.getCost());
		response.put("message", "Session already completed. No action taken.");
		return response;
	}

	private double calculateEnergyUsed(Session session) {
		Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
		long minutes = duration.toMinutes();
		return minutes * 0.075;
	}

	private void scheduleAutoStop(Long sessionId, int durationMin) {
		scheduler.schedule(() -> {
			try {
				stopSessionBySystem(sessionId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, durationMin, TimeUnit.MINUTES);
	}

	// Count total completed sessions
	public long getTotalSessions() {
		return sessionRepository.findAll().stream()
				.filter(s -> "completed".equalsIgnoreCase(s.getStatus()))
				.count();
	}

	// Sum total energy consumed (kWh)
	public double getTotalEnergyConsumed() {
		return sessionRepository.findAll().stream()
				.filter(s -> "completed".equalsIgnoreCase(s.getStatus()))
				.mapToDouble(Session::getEnergyKwh)
				.sum();
	}

	// Active Sessions - Sessions that are currently in progress
	public Long getActiveSessions() {
		return sessionRepository.findAll().stream()
				.filter(session -> "active".equalsIgnoreCase(session.getStatus()))
				.count();
	}

	// Average Uptime - Based on successful vs total sessions
	public Double getAverageUptime() {
		long totalSessions = sessionRepository.count();

		if (totalSessions == 0) {
			return 0.0;
		}

		long completedSessions = sessionRepository.findAll().stream()
				.filter(session -> "completed".equalsIgnoreCase(session.getStatus()))
				.count();

		double uptime = (completedSessions * 100.0) / totalSessions;
		return Math.round(uptime * 100.0) / 100.0; // Round to 2 decimals
	}
}
