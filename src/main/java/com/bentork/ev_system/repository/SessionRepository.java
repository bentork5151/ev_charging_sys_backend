package com.bentork.ev_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bentork.ev_system.model.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findTopByUserIdAndStatusOrderByStartTimeDesc(Long userId, String status);

}
