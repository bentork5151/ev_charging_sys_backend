package com.bentork.ev_system.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bentork.ev_system.model.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserId(Long userId);

    Optional<WalletTransaction> findTopByUserIdAndAmountAndSessionIdIsNullOrderByCreatedAtDesc(
            Long userId,
            BigDecimal amount);
}
