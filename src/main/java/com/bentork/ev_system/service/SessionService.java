package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.SessionDTO;
import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.model.Receipt;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.ReceiptRepository;
import com.bentork.ev_system.repository.SessionRepository;
import com.bentork.ev_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SessionService {

	@Autowired
	private final ReceiptRepository receiptRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChargerRepository chargerRepository;

	@Autowired
	@Lazy
	private ReceiptService receiptService;

	@Autowired
	private AdminNotificationService adminNotificationService;

	@Autowired
	private RevenueService revenueService;

	SessionService(ReceiptRepository receiptRepository) {
		this.receiptRepository = receiptRepository;
	}

	public Session startSessionFromReceipt(Receipt receipt, String boxId) {
		User user = receipt.getUser();
		Charger charger = receipt.getCharger();

		Session session = new Session();
		session.setUser(user);
		session.setCharger(charger);
		session.setBoxId(boxId);
		session.setStartTime(LocalDateTime.now());
		session.setStatus("initiated");
		session.setCreatedAt(LocalDateTime.now());

		sessionRepository.save(session);

		// Mock charger start logic
		boolean started = true;

		if (started) {
			session.setStatus("active");

			// link receipt with session
			receipt.setSession(session);
			receiptRepository.save(receipt);

			adminNotificationService.createSystemNotification("User '" + user.getName()
					+ "' started a session on charger '" + charger.getOcppId() + "' with BoxId: " + boxId,
					"SESSION_STARTED");

		} else {
			session.setStatus("failed");
		}

		return sessionRepository.save(session);
	}

	public void stopSession(Long userId, SessionDTO request) {
		// Fetch session
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new RuntimeException("Session not found"));

		if (!session.getUser().getId().equals(userId)) {
			throw new RuntimeException("Unauthorized to stop this session");
		}

		// Mark session as stopped
		session.setStatus("stopped");
		session.setEndTime(LocalDateTime.now());

		// Fetch linked receipt
		Receipt receipt = receiptRepository.findBySession(session)
				.orElseThrow(() -> new RuntimeException("Linked receipt not found"));

		// Simulate energy usage (replace with actual reading in production)
		double energyUsed = Math.round(Math.random() * 20 * 100.0) / 100.0; // 0–20 kWh, 2 decimals

		// Calculate final cost as BigDecimal (accurate), then convert to double for
		// session
		BigDecimal finalCostBD = BigDecimal.valueOf(energyUsed).multiply(receipt.getPlan().getRate()) // plan.getRate()
																										// is BigDecimal
				.setScale(2, RoundingMode.HALF_UP);

		double finalCost = finalCostBD.doubleValue();

		// Store energy + cost in session (session.cost is double)
		session.setEnergyKwh(energyUsed);
		session.setCost(finalCost);

		// Finalize receipt (receipt expects BigDecimal)
		receiptService.finalizeReceipt(session, finalCostBD);

		sessionRepository.save(session);

		// Admin notification
		adminNotificationService.createSystemNotification("User '" + session.getUser().getName()
				+ "' stopped session on charger '" + session.getCharger().getOcppId() + "'. Energy used: " + energyUsed
				+ " kWh, Final cost: " + finalCost, "SESSION_STOPPED");
	}

}
