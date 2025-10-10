package com.bentork.ev_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.dto.request.RFIDCardRequest;
import com.bentork.ev_system.model.RFIDCard;
import com.bentork.ev_system.service.RFIDCardService;

@RestController
@RequestMapping("/api/rfid-card")
public class RFIDCardController {

    @Autowired
    private RFIDCardService cardService;

    // Register card
    @PostMapping("/register")
    public ResponseEntity<RFIDCard> register(@RequestBody RFIDCardRequest req) {
        return ResponseEntity.ok(cardService.registerCard(req));
    }

    // Get all cards
    @GetMapping
    public ResponseEntity<List<RFIDCard>> getAll() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    // Get one card
    @GetMapping("/{id}")
    public ResponseEntity<RFIDCard> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    // Update card status
    @PutMapping("/{id}/status")
    public ResponseEntity<RFIDCard> updateStatus(
            @PathVariable Long id,
            @RequestBody RFIDCard reqCard) {
        // Only update status, ignore other fields
        RFIDCard updated = cardService.updateCardStatus(id, reqCard.isActive());
        return ResponseEntity.ok(updated);
    }

    // Delete card
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    // Total Cards
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalCards(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cardService.getTotalCards());
    }

    // Active Cards
    @GetMapping("/active")
    public ResponseEntity<Long> getActiveCards(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cardService.getActiveCards());
    }

    // Inactive Cards
    @GetMapping("/inactive")
    public ResponseEntity<Long> getInactiveCards(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cardService.getInactiveCards());
    }

    // Recently Added - Last 7 days
    @GetMapping("/recent")
    public ResponseEntity<Long> getRecentlyAddedCards(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cardService.getRecentlyAddedCards());
    }
}
