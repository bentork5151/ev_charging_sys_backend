package com.bentork.ev_system.controller;

import com.bentork.ev_system.dto.*;
import com.bentork.ev_system.dto.request.AdminLoginRequest;
import com.bentork.ev_system.dto.request.AdminSignupRequest;
import com.bentork.ev_system.dto.request.JwtResponse;
import com.bentork.ev_system.dto.request.UserLoginRequest;
import com.bentork.ev_system.dto.request.UserSignupRequest;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private AdminRepository adminRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/user/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserSignupRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmailOrMobile(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/admin/signup")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminSignupRequest request) {
        if (adminRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Admin email already in use");
        }
        Admin admin = new Admin();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setMobile(request.getMobile());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole("ADMIN");
        adminRepo.save(admin);
        return ResponseEntity.ok("Admin registered successfully");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmailOrMobile(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }
}