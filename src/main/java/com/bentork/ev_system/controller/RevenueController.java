package com.bentork.ev_system.controller;

import com.bentork.ev_system.config.JwtUtil;
import com.bentork.ev_system.dto.request.RevenueDTO;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.service.RevenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
public class RevenueController {

    private final RevenueService revenueService;
    private final JwtUtil jwtUtil;
    private final AdminRepository adminRepository;

    public RevenueController(RevenueService revenueService, JwtUtil jwtUtil, AdminRepository adminRepository) {
        this.revenueService = revenueService;
        this.jwtUtil = jwtUtil;
        this.adminRepository = adminRepository;
    }

    private void ensureAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Missing/invalid Authorization header");
        String email = jwtUtil.extractUsername(authHeader.substring(7));
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        // presence is enough; add role checks if you store roles in JWT
    }

    @GetMapping("/all")
    public ResponseEntity<List<RevenueDTO>> all(@RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getAllRevenue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RevenueDTO> byId(@PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id,
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        revenueService.delete(id);
        return ResponseEntity.ok("Revenue deleted");
    }

    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getTotalRevenue());
    }

    // Pending Revenue
    @GetMapping("/pending")
    public ResponseEntity<BigDecimal> getPendingRevenue(
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getPendingRevenue());
    }

    // Total Transactions
    @GetMapping("/transactions/total")
    public ResponseEntity<Long> getTotalTransactions(
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getTotalTransactions());
    }

    // Success Rate
    @GetMapping("/success-rate")
    public ResponseEntity<Double> getSuccessRate(
            @RequestHeader("Authorization") String auth) {
        ensureAdmin(auth);
        return ResponseEntity.ok(revenueService.getSuccessRate());
    }

}
