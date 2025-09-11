package com.bentork.ev_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.RFIDCard;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.RFIDCardRepository;
import com.bentork.ev_system.repository.SessionRepository;
import com.bentork.ev_system.repository.UserRepository;

@Service
public class RFIDChargingService {

    @Autowired
    private RFIDCardRepository cardRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private ChargerRepository chargerRepo;
    @Autowired
    private SessionRepository sessionRepo;
    @Autowired
    private UserNotificationService notificationService;
    @Autowired
    private AdminNotificationService adminNotificationService;

    // Start charging
    public Session startCharging(String cardNumber, Long chargerId, String boxId) {
        RFIDCard card = cardRepo.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Invalid RFID card"));

        if (!card.isActive())
            throw new RuntimeException("Card is not active");

        User user = card.getUser();
        if (user.getWalletBalance().compareTo(BigDecimal.ONE) < 0)
            throw new RuntimeException("Insufficient balance");

        Charger charger = chargerRepo.findById(chargerId)
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        Session session = new Session();
        session.setUser(user);
        session.setCharger(charger);
        session.setStatus("IN_PROGRESS");
        session.setStartTime(LocalDateTime.now());
        session.setEnergyKwh(0.0);
        session.setCost(0.0);
        session.setCreatedAt(LocalDateTime.now());
        session.setBoxId(boxId);

        Session saved = sessionRepo.save(session);

        // Notify admins about session start
        adminNotificationService.createSystemNotification(
                "Charging session started for user " + user.getName() +
                        " on charger " + charger.getId(),
                "SESSION_START");

        return saved;
    }

    // Update energy
    public Session updateEnergy(Long sessionId, BigDecimal currentKwh) {
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus()))
            return session;

        BigDecimal previous = BigDecimal.valueOf(session.getEnergyKwh());
        BigDecimal delta = currentKwh.subtract(previous);

        if (delta.compareTo(BigDecimal.ZERO) <= 0)
            return session;

        Charger charger = session.getCharger();
        BigDecimal cost = delta.multiply(BigDecimal.valueOf(charger.getRate()));

        User user = session.getUser();

        if (user.getWalletBalance().compareTo(cost) < 0) {
            // Notify user about insufficient balance
            notificationService.createNotification(
                    user.getId(),
                    "Charging Stopped - Low Balance",
                    "Your charging session has been stopped due to insufficient wallet balance. Please top up to continue.",
                    "WALLET");
            return stopCharging(sessionId); // auto-stop
        }

        // Deduct from wallet
        user.setWalletBalance(user.getWalletBalance().subtract(cost));

        // Update session data
        session.setEnergyKwh(currentKwh.doubleValue());
        session.setCost(session.getCost() + cost.doubleValue());

        userRepo.save(user);
        return sessionRepo.save(session);
    }

    // Stop charging
    public Session stopCharging(Long sessionId) {
        Session session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!"IN_PROGRESS".equals(session.getStatus()))
            return session;

        session.setStatus("COMPLETED");
        session.setEndTime(LocalDateTime.now());

        System.out.println(" Relay OFF → Charger " + session.getCharger().getId());

        Session saved = sessionRepo.save(session);

        // Notify admins about session end
        adminNotificationService.createSystemNotification(
                "Charging session completed for User " + session.getUser().getName() +
                        ". Total cost: ₹" + session.getCost(),
                "SESSION_END");

        return saved;
    }
}
