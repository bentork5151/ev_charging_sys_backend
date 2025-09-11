
package com.bentork.ev_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bentork.ev_system.model.User;
import com.bentork.ev_system.model.UserNotification;
import com.bentork.ev_system.repository.UserNotificationRepository;
import com.bentork.ev_system.repository.UserRepository;

@Service
public class UserNotificationService {

    private final UserNotificationRepository repository;
    private final UserRepository userRepo;

    public UserNotificationService(UserNotificationRepository repository, UserRepository userRepo) {
        this.repository = repository;
        this.userRepo = userRepo;
    }

    // Get all notifications for a user
    public List<UserNotification> getUserNotifications(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repository.findByUser(user);
    }

    // Get only unread notifications
    public List<UserNotification> getUnreadNotifications(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return repository.findByUserAndIsReadFalse(user);
    }

    // Create and save new notification
    public UserNotification createNotification(Long userId, String title, String message, String type) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserNotification notification = new UserNotification();
        notification.setUser(user); // Managed user entity
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return repository.save(notification);
    }

    // Mark notification as read
    public Optional<UserNotification> markAsRead(Long notificationId) {
        Optional<UserNotification> optional = repository.findById(notificationId);
        if (optional.isPresent()) {
            UserNotification notification = optional.get();
            notification.setIsRead(true);
            repository.save(notification);
        }
        return optional;
    }
}
