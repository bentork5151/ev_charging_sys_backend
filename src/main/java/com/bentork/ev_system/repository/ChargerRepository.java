package com.bentork.ev_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bentork.ev_system.model.Charger;

public interface ChargerRepository extends JpaRepository<Charger, Long> {
    boolean existsByOcppId(String ocppId);
}
