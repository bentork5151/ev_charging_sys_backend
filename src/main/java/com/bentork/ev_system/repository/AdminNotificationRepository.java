package com.bentork.ev_system.repository;

import com.bentork.ev_system.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {
}
