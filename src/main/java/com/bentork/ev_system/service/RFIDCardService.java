package com.bentork.ev_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.dto.request.RFIDCardRequest;
import com.bentork.ev_system.model.RFIDCard;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.RFIDCardRepository;
import com.bentork.ev_system.repository.UserRepository;

@Service
public class RFIDCardService {

    @Autowired
    private RFIDCardRepository cardRepo;
    @Autowired
    private UserRepository userRepo;

    // Register new RFID card
    public RFIDCard registerCard(RFIDCardRequest req) {
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RFIDCard card = new RFIDCard();
        card.setCardNumber(req.getCardNumber());
        card.setUser(user);
        card.setActive(true);

        return cardRepo.save(card);
    }

    // Get all cards
    public List<RFIDCard> getAllCards() {
        return cardRepo.findAll();
    }

    // Get card by ID
    public RFIDCard getCard(Long id) {
        return cardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }

    // Update card status (activate/deactivate)
    public RFIDCard updateCardStatus(Long id, boolean active) {
        RFIDCard card = getCard(id);
        card.setActive(active);
        return cardRepo.save(card);
    }

    // Delete card
    public void deleteCard(Long id) {
        cardRepo.deleteById(id);
    }

    // Total Cards
    public Long getTotalCards() {
        return cardRepo.count();
    }

    // Active Cards
    public Long getActiveCards() {
        return cardRepo.findAll().stream()
                .filter(RFIDCard::isActive)
                .count();
    }

    // Inactive Cards
    public Long getInactiveCards() {
        return cardRepo.findAll().stream()
                .filter(card -> !card.isActive())
                .count();
    }

    // Recently Added - Last 7 days
    public Long getRecentlyAddedCards() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        return cardRepo.findAll().stream()
                .filter(card -> card.getCreatedAt() != null
                        && card.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
    }
}
