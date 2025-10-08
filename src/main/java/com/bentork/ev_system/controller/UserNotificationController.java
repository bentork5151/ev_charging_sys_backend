
package com.bentork.ev_system.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bentork.ev_system.model.UserNotification;
import com.bentork.ev_system.service.UserNotificationService;

@RestController
@RequestMapping("/api/notifications")
public class UserNotificationController {

    private final UserNotificationService service;

    public UserNotificationController(UserNotificationService service) {
        this.service = service;
    }

    // Get all notifications for a user
    @GetMapping("/user/{userId}")
    public List<UserNotification> getUserNotifications(@PathVariable Long userId) {
        return service.getUserNotifications(userId);
    }

    // Get only unread notifications
    @GetMapping("/user/{userId}/unread")
    public List<UserNotification> getUnreadNotifications(@PathVariable Long userId) {
        return service.getUnreadNotifications(userId);
    }

    // Create a notification for a user
    @PostMapping("/user/{userId}")
    public UserNotification createNotification(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {

        String title = payload.get("title");
        String message = payload.get("message");
        String type = payload.get("type");

        return service.createNotification(userId, title, message, type);
    }

    // Mark a notification as read
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<UserNotification> markAsRead(@PathVariable Long notificationId) {
        Optional<UserNotification> updated = service.markAsRead(notificationId);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
