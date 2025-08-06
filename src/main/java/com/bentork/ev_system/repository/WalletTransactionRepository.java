package com.bentork.ev_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bentork.ev_system.model.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByUserId(Long userId);
}
