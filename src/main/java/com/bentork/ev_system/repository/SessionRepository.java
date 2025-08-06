package com.bentork.ev_system.repository;

import com.bentork.ev_system.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findTopByUserIdAndStatusOrderByStartTimeDesc(Long userId, String status);
}
