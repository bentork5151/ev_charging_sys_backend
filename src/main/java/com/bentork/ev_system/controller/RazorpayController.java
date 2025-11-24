
package com.bentork.ev_system.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bentork.ev_system.model.WalletTransaction;
import com.bentork.ev_system.service.RazorpayService;
import com.bentork.ev_system.service.WalletTransactionService;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api/razorpay")
public class RazorpayController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private WalletTransactionService walletService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> payload) {
        try {
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            String order = razorpayService.createOrder(amount);

            JSONObject orderObj = new JSONObject(order);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", orderObj.getString("id"));
            response.put("amount", orderObj.getInt("amount"));
            response.put("currency", orderObj.getString("currency"));
            response.put("receipt", orderObj.getLong("receipt"));
            
            return ResponseEntity.ok(response);
        } catch (RazorpayException | NumberFormatException | JSONException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String orderId = payload.get("razorpay_order_id");
            String paymentId = payload.get("razorpay_payment_id");
            String signature = payload.get("razorpay_signature");
            String userIdStr = payload.get("user_id");

            boolean valid = razorpayService.verifySignature(orderId, paymentId, signature);

            if (!valid) {
                return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "message", "Invalid payment signature"
                ));
            }

            // ✅ Fetch actual amount from Razorpay order object
            com.razorpay.Order razorOrder = razorpayService.fetchOrder(orderId);
            BigDecimal amount = new BigDecimal(razorOrder.get("amount").toString())
                    .divide(BigDecimal.valueOf(100)); // Convert paise to rupees

            WalletTransaction tx = new WalletTransaction();
            tx.setUserId(Long.parseLong(userIdStr));
            tx.setAmount(amount);
            tx.setType("credit");
            tx.setMethod("Razorpay");
            tx.setStatus("success");
            tx.setTransactionRef(paymentId);

            walletService.save(tx);

            return ResponseEntity.ok(Map.of(
                    "verified", true,
                    "message", "Payment successful and wallet updated.",
                    "paymentId", paymentId,
                    "orderId", orderId,
                    "amount", amount));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "verifiied", false,
                "message", "Error verifying payment " + e.getMessage()
            ));
        }
    }
}
