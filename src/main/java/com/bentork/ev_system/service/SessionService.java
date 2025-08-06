package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.SessionDTO;
import com.bentork.ev_system.mapper.SessionMapper;
import com.bentork.ev_system.model.Charger;
import com.bentork.ev_system.model.Session;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.ChargerRepository;
import com.bentork.ev_system.repository.SessionRepository;
import com.bentork.ev_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private AdminNotificationService adminNotificationService;


    public SessionDTO startSession(Long userId, SessionDTO request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Charger charger = chargerRepository.findById(request.getChargerId())
                .orElseThrow(() -> new RuntimeException("Charger not found"));

        Session session = new Session();
        session.setUser(user);
        session.setCharger(charger);
        session.setBoxId(request.getBoxId());
        session.setStartTime(LocalDateTime.now());
        session.setStatus("in_progress");
        session.setCreatedAt(LocalDateTime.now());

        sessionRepository.save(session);

        // 🔔 Admin notification: session started
        adminNotificationService.createSystemNotification(
                "User '" + user.getName() + "' started a session on charger '" + charger.getOcppId() + "' at " + LocalDateTime.now(),
                "SESSION_STARTED"
        );

        SessionDTO response = new SessionDTO();
        response.setSessionId(session.getId());
        response.setMessage("Charging session started");
        response.setStatus("in_progress");
        return response;
    }

    public SessionDTO stopSession(Long userId, SessionDTO request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to stop this session");
        }

        session.setEndTime(LocalDateTime.now());
        session.setStatus("complete");

        double energyUsed = calculateEnergyUsed(session);
        double cost = energyUsed * 25.0;

        session.setEnergyKwh(energyUsed);
        session.setCost(cost);

        sessionRepository.save(session);

        // 🔔 Admin notification: session ended
        User user = session.getUser();
        Charger charger = session.getCharger();

        adminNotificationService.createSystemNotification(
                "User '" + user.getName() + "' ended session on charger '" + charger.getOcppId() + "' at " + session.getEndTime()
                        + ". Energy used: " + String.format("%.2f", energyUsed) + " kWh, Cost: ₹" + String.format("%.2f", cost),
                "SESSION_ENDED"
        );

        SessionDTO response = new SessionDTO();
        response.setSessionId(session.getId());
        response.setMessage("Charging session completed");
        response.setEnergyUsed(energyUsed);
        response.setCost(cost);
        response.setStatus("complete");
        return response;
    }

    private double calculateEnergyUsed(Session session) {
        Duration duration = Duration.between(session.getStartTime(), session.getEndTime());
        long minutes = duration.toMinutes();
        return minutes * 0.075; // Example: 0.075 kWh/min
    }
}
