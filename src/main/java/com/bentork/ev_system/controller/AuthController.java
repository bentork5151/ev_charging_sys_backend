package com.bentork.ev_system.controller;

//import com.bentork.ev_system.dto.*;
import com.bentork.ev_system.dto.request.AdminLoginRequest;
import com.bentork.ev_system.dto.request.AdminSignupRequest;
import com.bentork.ev_system.dto.request.JwtResponse;
import com.bentork.ev_system.dto.request.UserLoginRequest;
import com.bentork.ev_system.dto.request.UserSignupRequest;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.User;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.UserRepository;
import com.bentork.ev_system.service.AdminNotificationService;
import com.bentork.ev_system.service.OtpDeliveryService;
import com.bentork.ev_system.service.OtpService;
import com.bentork.ev_system.config.JwtUtil;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private AdminRepository adminRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private OtpDeliveryService otpDeliveryService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private AdminNotificationService adminNotificationService;

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
        userRepo.save(user);

        // 🔔 Admin notification: User Registered
        adminNotificationService.notifyNewUserRegistration(user.getName());

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmailOrMobile(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    // Total users - admin only
    @GetMapping("/users/total")
    public ResponseEntity<Long> getTotalUsers(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(userRepo.count());
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
                new UsernamePasswordAuthenticationToken(request.getEmailOrMobile(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/user/request-otp")
    public ResponseEntity<?> requestOtp(@RequestParam String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.badRequest().body("Email not found");

        String otp = otpService.generateOtp(email);
        otpDeliveryService.sendOtp(email, otp);
        return ResponseEntity.ok("OTP sent to your email.");
    }

    @PostMapping("/user/reset-password")
    public ResponseEntity<?> resetPasswordViaOtp(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword) {
        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        otpService.clearOtp(email);

        return ResponseEntity.ok("Password reset successful.");
    }

    @PostMapping("/admin/request-otp")
    public ResponseEntity<?> requestAdminOtp(@RequestParam String email) {
        Optional<Admin> adminOpt = adminRepo.findByEmail(email);
        if (adminOpt.isEmpty())
            return ResponseEntity.badRequest().body("Admin email not found");

        String otp = otpService.generateOtp(email);
        otpDeliveryService.sendOtp(email, otp);
        return ResponseEntity.ok("OTP sent to admin email.");
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetAdminPasswordViaOtp(
            @RequestParam String otp,
            @RequestParam String newPassword) {
        String email = otpService.getEmailByOtp(otp);
        if (email == null) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        Optional<Admin> adminOpt = adminRepo.findByEmail(email);
        if (adminOpt.isEmpty())
            return ResponseEntity.badRequest().body("Admin not found");

        Admin admin = adminOpt.get();
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepo.save(admin);
        otpService.clearOtp(email);

        return ResponseEntity.ok("Password reset successful.");
    }

    @GetMapping("/user/google-login-success")
    public ResponseEntity<?> googleLoginSuccess(OAuth2AuthenticationToken authToken) {
        String email = authToken.getPrincipal().getAttribute("email");
        String name = authToken.getPrincipal().getAttribute("name");

        Optional<User> optionalUser = userRepo.findByEmail(email);
        User user = optionalUser.orElseGet(() -> {
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPassword(""); // No password as it's Google login
            return userRepo.save(newUser);
        });

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), "", Collections.emptyList());
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

}