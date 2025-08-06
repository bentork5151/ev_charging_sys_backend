package com.bentork.ev_system.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.model.WalletTransaction;
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.repository.WalletTransactionRepository;

@Service
public class WalletTransactionService {
    @Autowired
    private WalletTransactionRepository repo;
    @Autowired
    private UserRepository userRepo;

    public WalletTransaction save(WalletTransaction tx) {
        WalletTransaction saved = repo.save(tx);

        if ("success".equalsIgnoreCase(saved.getStatus())) {
            userRepo.findById(saved.getUserId()).ifPresent(user -> {
                BigDecimal balance = user.getWalletBalance() != null ? user.getWalletBalance() : BigDecimal.ZERO;
                if ("credit".equalsIgnoreCase(saved.getType())) {
                    user.setWalletBalance(balance.add(saved.getAmount()));
                } else {
                    user.setWalletBalance(balance.subtract(saved.getAmount()));
                }
                userRepo.save(user);
            });
        }

        return saved;
    }
}
