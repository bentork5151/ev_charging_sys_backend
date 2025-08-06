package com.bentork.ev_system.controller;

import com.bentork.ev_system.config.JwtUtil;
import com.bentork.ev_system.dto.request.SessionDTO;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/start")
    public ResponseEntity<String> startSession(
            @RequestBody SessionDTO request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sessionService.startSession(user.getId(), request);
        return ResponseEntity.ok("Session started successfully.");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopSession(
            @RequestBody SessionDTO request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sessionService.stopSession(user.getId(), request);
        return ResponseEntity.ok("Session stopped successfully.");
    }
}
