package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.RevenueDTO;
import com.bentork.ev_system.mapper.RevenueMapper;
import com.bentork.ev_system.model.Revenue;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.repository.RevenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RevenueService {

    private final RevenueRepository revenueRepository;

    public RevenueService(RevenueRepository revenueRepository) {
        this.revenueRepository = revenueRepository;
    }

    /* package-private */
    Revenue recordRevenueForSession(Session session,
            double amount,
            String paymentMethod,
            String transactionId,
            String paymentStatus) {
        Revenue revenue = RevenueMapper.fromSession(
                session,
                paymentMethod,
                transactionId != null ? transactionId : "TXN-" + UUID.randomUUID(),
                paymentStatus != null ? paymentStatus : "success",
                amount);
        return revenueRepository.save(revenue);
    }

    // Calculate Total Revenue
    public BigDecimal getTotalRevenue() {
        double total = revenueRepository.findAll().stream()
                .filter(r -> "SUCCESS".equalsIgnoreCase(r.getPaymentStatus()))
                .mapToDouble(Revenue::getAmount)
                .sum();

        return BigDecimal.valueOf(total);
    }

    // Pending Revenue (₹219)
    public BigDecimal getPendingRevenue() {
        double pending = revenueRepository.findAll().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getPaymentStatus()))
                .mapToDouble(Revenue::getAmount)
                .sum();

        return BigDecimal.valueOf(pending);
    }

    public List<RevenueDTO> getAllRevenue() {
        return revenueRepository.findAll().stream()
                .map(RevenueMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RevenueDTO getById(Long id) {
        Revenue r = revenueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Revenue not found"));
        return RevenueMapper.toDTO(r);
    }

    public void delete(Long id) {
        if (!revenueRepository.existsById(id))
            throw new RuntimeException("Revenue not found");
        revenueRepository.deleteById(id);
    }

    // Total Transactions
    public Long getTotalTransactions() {
        return revenueRepository.count();
    }

    // Success Rate
    public Double getSuccessRate() {
        long totalTransactions = revenueRepository.count();

        if (totalTransactions == 0) {
            return 0.0;
        }

        long successfulTransactions = revenueRepository.findAll().stream()
                .filter(r -> "SUCCESS".equalsIgnoreCase(r.getPaymentStatus()))
                .count();

        double successRate = (successfulTransactions * 100.0) / totalTransactions;
        return Math.round(successRate * 100.0) / 100.0; // Round to 2 decimals
    }
}