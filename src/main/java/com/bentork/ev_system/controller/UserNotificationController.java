package com.bentork.ev_system.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.model.User;
import com.bentork.ev_system.model.UserNotification;
import com.bentork.ev_system.service.UserNotificationService;

@RestController
@RequestMapping("/api/notifications")
public class UserNotificationController {

    private final UserNotificationService service;

    public UserNotificationController(UserNotificationService service) {
        this.service = service;
    }

    // In real apps, get user from JWT or session
    @GetMapping("/user/{userId}")
    public List<UserNotification> getUserNotifications(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return service.getUserNotifications(user);
    }

    @GetMapping("/user/{userId}/unread")
    public List<UserNotification> getUnreadNotifications(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return service.getUnreadNotifications(user);
    }

    @PostMapping("/user/{userId}")
    public UserNotification createNotification(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {

        User user = new User();
        user.setId(userId);

        String title = payload.get("title");
        String message = payload.get("message");
        String type = payload.get("type");

        return service.createNotification(user, title, message, type);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<UserNotification> markAsRead(@PathVariable Long notificationId) {
        Optional<UserNotification> updated = service.markAsRead(notificationId);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}