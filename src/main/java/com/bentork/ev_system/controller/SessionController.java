package com.bentork.ev_system.controller;

import com.bentork.ev_system.config.JwtUtil;
import com.bentork.ev_system.dto.request.SessionDTO;
import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.model.Receipt;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.PlanRepository;
import com.bentork.ev_system.repository.ReceiptRepository;
import com.bentork.ev_system.repository.SessionRepository;
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.service.ReceiptService;
import com.bentork.ev_system.service.SessionService;
import com.bentork.ev_system.service.WalletTransactionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChargerRepository chargerRepository;

	@Autowired
	private ReceiptService receiptService;
	
	@Autowired
	private ReceiptRepository receiptRepository;
	
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private WalletTransactionService walletTransactionService;

	@PostMapping("/start")
	public ResponseEntity<SessionDTO> startSession(@RequestBody SessionDTO request,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7); // Remove "Bearer "
		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Plan plan = planRepository.findById(request.getPlanId())
				.orElseThrow(() -> new RuntimeException("Plan with id " + request.getPlanId() + " not found"));

		Charger charger = chargerRepository.findById(request.getChargerId())
				.orElseThrow(() -> new RuntimeException("Charger not found"));

		// Create pending receipt
		Receipt receipt = receiptService.createReceipt(user, plan, charger);

		// 4️ Check wallet balance
		if (!walletTransactionService.hasBalance(user.getId(), receipt.getAmount())) {
			throw new RuntimeException("Insufficient wallet balance. Please top-up and retry.");
		}

		// 5️ Deduct wallet & mark receipt as PAID
		walletTransactionService.debit(user.getId(), null, receipt.getAmount(), "CHARGING_PAYMENT");
		receipt.setStatus("PAID");
		receiptService.save(receipt);

		// 6️ Start session
		Session session = sessionService.startSessionFromReceipt(receipt, request.getBoxId());
		receipt.setSession(session);

		// 7️ Handle charger failure → refund & mark REFUNDED
		if ("failed".equalsIgnoreCase(session.getStatus())) {
			walletTransactionService.credit(user.getId(), null, receipt.getAmount(), "CHARGING_REFUND");
			receipt.setStatus("REFUNDED");
			receiptService.save(receipt);
			throw new RuntimeException("Charger failed to start. Payment refunded.");
		}
		// 8️ Build response
		SessionDTO response = new SessionDTO();
		response.setSessionId(session.getId());
		response.setMessage("Charging session started successfully.");
		response.setStatus(session.getStatus());
		response.setUserId(user.getId());
		response.setChargerId(charger.getId());
		response.setBoxId(session.getBoxId());
		response.setPlanId(plan.getId()); // plan you fetched earlier
		response.setEnergyUsed(session.getEnergyKwh());
		response.setCost(session.getCost());

		return ResponseEntity.ok(response);

	}

	@PostMapping("/stop")
	public ResponseEntity<SessionDTO> stopSession(@RequestBody SessionDTO request,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);
		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		// service updates session in DB
		sessionService.stopSession(user.getId(), request);

		// fetch session again to build response
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new RuntimeException("Session not found after stop"));

		// build DTO
		SessionDTO response = new SessionDTO();
		response.setSessionId(session.getId());
		response.setMessage("Charging session stopped successfully.");
		response.setStatus(session.getStatus());
		response.setUserId(user.getId());
		response.setChargerId(session.getCharger().getId());
		response.setBoxId(session.getBoxId());

		// ✅ planId safely from receipt
		receiptRepository.findBySession(session).ifPresent(receipt -> {
			response.setPlanId(receipt.getPlan().getId());
		});

		response.setEnergyUsed(session.getEnergyKwh());
		response.setCost(session.getCost()); // ✅ now BigDecimal, no mismatch

		return ResponseEntity.ok(response);
	}

}
