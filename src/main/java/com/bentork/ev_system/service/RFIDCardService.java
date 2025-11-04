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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RFIDCardService {

    @Autowired
    private RFIDCardRepository cardRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SteveDatabaseService steveDatabaseService;

    // Register new RFID card
    public RFIDCard registerCard(RFIDCardRequest req) {
        log.info("Registering new RFID card: {}", req.getCardNumber());

        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RFIDCard card = new RFIDCard();
        card.setCardNumber(req.getCardNumber());
        card.setUser(user);
        card.setActive(true);

        RFIDCard savedCard = cardRepo.save(card);
        log.info("Card saved in our database: {}", savedCard.getCardNumber());

        boolean pushedToSteve = steveDatabaseService.addOcppTag(savedCard.getCardNumber(), user);
        if (!pushedToSteve) {
            log.error("CRITICAL: Failed to register card {} in SteVe! User won't be able to charge! Deleting Credentials from our database", savedCard.getCardNumber());

            deleteCard(savedCard.getId());
            log.error("Delete successfull card number: {}", savedCard.getCardNumber());
        }

        return savedCard;
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
        log.info("Updating card {} status to: {}", id, active ? "ACTIVE" : "BLOCKED");

        RFIDCard card = cardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setActive(active);
        RFIDCard updated = cardRepo.save(card);

        log.info("Card {} updated", card.getCardNumber());

        // boolean updatedInSteve = ocppTagSyncService.updateCardInSteve(card.getCardNumber(), !active);
        // if (!updatedInSteve) {
        //     log.error("CRITICAL: Failed to update card {} status in SteVe!", card.getCardNumber());
        // }
        return updated;
    }

    // Delete card
    public void deleteCard(Long id) {
        log.info("Deleting card {}", id);

        RFIDCard card = cardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        cardRepo.delete(card);
        log.info("Card {} deleted from our database", card.getCardNumber());

        // boolean blockedInSteve = ocppTagSyncService.updateCardInSteve(card.getCardNumber(), true);
        // if (!blockedInSteve) {
        //     log.warn("Could not block card {} in SteVe", card.getCardNumber());
        // }
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
