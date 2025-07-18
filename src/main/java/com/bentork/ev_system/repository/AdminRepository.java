package com.bentork.ev_system.repository;

import com.bentork.ev_system.model.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmailOrMobile(String email, String mobile);
    boolean existsByEmail(String email);
}

