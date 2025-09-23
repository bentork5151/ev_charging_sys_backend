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
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.service.ReceiptService;
import com.bentork.ev_system.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

	@Autowired
	private SessionService sessionService;

	@Autowired
	private ReceiptService receiptService;

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private ChargerRepository chargerRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	/**
	 * Start session using prepaid plan OR kWh package/custom.
	 * Creates receipt → debits wallet → starts session.
	 * Returns receiptId, sessionId, and amount debited.
	 */
	@PostMapping("/start")
	public ResponseEntity<Map<String, Object>> startSession(
			@RequestBody SessionDTO request,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);
		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Charger charger = chargerRepository.findById(request.getChargerId())
				.orElseThrow(() -> new RuntimeException("Charger not found"));

		Plan plan = null;
		if (request.getPlanId() != null) {
			plan = planRepository.findById(request.getPlanId())
					.orElseThrow(() -> new RuntimeException("Plan not found"));
		}

		Receipt receipt = receiptService.createReceipt(user, plan, charger, request.getSelectedKwh());
		Receipt paidReceipt = receiptService.payReceipt(receipt.getId(), request.getBoxId());
		Session session = paidReceipt.getSession();

		return ResponseEntity.ok(Map.of(
				"receiptId", paidReceipt.getId(),
				"sessionId", session.getId(),
				"amountDebited", paidReceipt.getAmount(),
				"message", "Session started successfully."));
	}

	/**
	 * Stop session, finalize cost and return energy used, refund/extra flags.
	 */
	@PostMapping("/stop")
	public ResponseEntity<Map<String, Object>> stopSession(
			@RequestBody SessionDTO request,
			@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);
		String email = jwtUtil.extractUsername(token);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Map<String, Object> result = sessionService.stopSession(user.getId(), request);
		return ResponseEntity.ok(result);
	}
}
