package com.bentork.ev_system.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.bentork.ev_system.model.User;
import com.bentork.ev_system.model.UserNotification;
import com.bentork.ev_system.repository.UserNotificationRepository;

@Service
public class UserNotificationService {
    private final UserNotificationRepository repository;

    public UserNotificationService(UserNotificationRepository repository) {
        this.repository = repository;
    }

    public List<UserNotification> getUserNotifications(User user) {
        return repository.findByUser(user);
    }

    public List<UserNotification> getUnreadNotifications(User user) {
        return repository.findByUserAndIsReadFalse(user);
    }

    public UserNotification createNotification(User user, String title, String message, String type) {
        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return repository.save(notification);
    }

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
