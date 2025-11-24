package com.bentork.ev_system.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.dto.request.PlanDTO;
import com.bentork.ev_system.mapper.PlanMapper;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.PlanRepository;

@RestController
@RequestMapping("/api/plans")
@PreAuthorize("hasAuthority('ADMIN')")
public class PlanController {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private AdminRepository adminRepository;

    // ✅ Add Plan
    @PostMapping("/add")
    public ResponseEntity<?> addPlan(@RequestBody PlanDTO dto, Authentication authentication) {
        String adminEmail = authentication.getName();
        Optional<Admin> admin = adminRepository.findByEmail(adminEmail);
        if (admin.isEmpty()) {
            return ResponseEntity.status(403).body("Admin not found");
        }

        Plan plan = PlanMapper.toEntity(dto, admin.get());
        planRepository.save(plan);
        return ResponseEntity.ok("Plan Created");
    }

    // ✅ Get All Plans (DTO version to avoid lazy loading issues)
    @GetMapping("/all")
    public ResponseEntity<?> getAllPlans() {
        try {
            List<Plan> plans = planRepository.findAll();
            List<PlanDTO> dtos = plans.stream()
                    .map(PlanMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch plans");
        }
    }


    // ✅ Get Plan by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        Optional<Plan> optionalPlan = planRepository.findById(id);

        if (optionalPlan.isEmpty()) {
            return ResponseEntity.status(404).body("Plan not found");
        }

        PlanDTO dto = PlanMapper.toDTO(optionalPlan.get());
        return ResponseEntity.ok(dto);
    }





    // ✅ Update Plan
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePlan(@PathVariable Long id, @RequestBody PlanDTO updatedDto, Authentication authentication) {
        Optional<Plan> optionalPlan = planRepository.findById(id);
        if (optionalPlan.isEmpty()) {
            return ResponseEntity.status(404).body("Plan not found");
        }

        String adminEmail = authentication.getName();
        Optional<Admin> admin = adminRepository.findByEmail(adminEmail);
        if (admin.isEmpty()) {
            return ResponseEntity.status(403).body("Admin not found");
        }

        Plan updated = PlanMapper.updateEntity(optionalPlan.get(), updatedDto, admin.get());
        planRepository.save(updated);
        return ResponseEntity.ok("Plan Updated");
    }

    // ✅ Delete Plan
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        if (planRepository.existsById(id)) {
            planRepository.deleteById(id);
            return ResponseEntity.ok("Plan Deleted");
        } else {
            return ResponseEntity.status(404).body("Plan not found");
        }
    }
}
