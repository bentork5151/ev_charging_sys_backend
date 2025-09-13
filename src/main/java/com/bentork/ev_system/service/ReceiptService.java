package com.bentork.ev_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.model.Receipt;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.ReceiptRepository;

@Service
public class ReceiptService {

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private WalletTransactionService walletTransactionService;

	@Autowired
	@Lazy
	private SessionService sessionService;

	// Create pending receipt
	public Receipt createReceipt(User user, Plan plan, Charger charger) {

		Receipt receipt = new Receipt();
		receipt.setUser(user);
		receipt.setPlan(plan);
		receipt.setCharger(charger);
		receipt.setAmount(plan.getRate());
		receipt.setStatus("PENDING");
		receipt.setCreatedAt(LocalDateTime.now());
		return receiptRepository.save(receipt);
	}

	public Receipt payReceipt(Long receoptId, String boxId) {

		Receipt receipt = receiptRepository.findById(receoptId)
				.orElseThrow(() -> new RuntimeException("Receipt not found"));

		Long userId = receipt.getUser().getId();
		BigDecimal amount = receipt.getAmount();

		if (!walletTransactionService.hasBalance(userId, amount)) {
			throw new RuntimeException("Insuffisiant walllet balence.Please to-up");

		}

		walletTransactionService.debit(userId, null, amount, "Charging Payment");

		receipt.setStatus("PAID");
		receipt.setUpdatedAt(LocalDateTime.now());
		receiptRepository.save(receipt);

		Session session = sessionService.startSessionFromReceipt(receipt, boxId);
		receipt.setSession(session);

		if ("failed".equalsIgnoreCase(session.getStatus())) {
			walletTransactionService.credit(userId, session.getId(), amount, "CHARGING_REFUND");
			receipt.setStatus("REFUNDED");
		}

		return receiptRepository.save(receipt);
	}

	public void finalizeReceipt(Session session, BigDecimal finalCost) {
		Receipt receipt = receiptRepository.findBySession(session)
				.orElseThrow(() -> new RuntimeException("Linked receipt not found"));

		receipt.setStatus("FINALIZED");
		receipt.setUpdatedAt(LocalDateTime.now());
		receiptRepository.save(receipt);

		// If actual cost < prepaid → refund difference
		if (finalCost.compareTo(receipt.getAmount()) < 0) {
			BigDecimal refund = receipt.getAmount().subtract(finalCost);
			walletTransactionService.credit(session.getUser().getId(), session.getId(), refund,
					"CHARGING_SETTLE_REFUND");
		}

		// If actual cost > prepaid → debit extra
		if (finalCost.compareTo(receipt.getAmount()) > 0) {
			BigDecimal extra = finalCost.subtract(receipt.getAmount());
			walletTransactionService.debit(session.getUser().getId(), session.getId(), extra, "CHARGING_SETTLE_DEBIT");
		}
	}

	public Receipt save(Receipt receipt) {
		return receiptRepository.save(receipt);
	}
}
