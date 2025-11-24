package com.bentork.ev_system.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bentork.ev_system.dto.request.PlanDTO;
import com.bentork.ev_system.dto.request.UserPlanSelectionDTO;
import com.bentork.ev_system.mapper.PlanMapper;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.repository.PlanRepository;
import com.bentork.ev_system.service.UserPlanSelectionService;

@RestController
@RequestMapping("/api/user-plan-selection")
public class UserPlanSelectionController {

    @Autowired
    private PlanRepository planRepository;

    private final UserPlanSelectionService userPlanSelectionService;

    public UserPlanSelectionController(UserPlanSelectionService userPlanSelectionService) {
        this.userPlanSelectionService = userPlanSelectionService;
    }

    @PostMapping("/select")
    public ResponseEntity<UserPlanSelectionDTO> selectPlan(@RequestBody UserPlanSelectionDTO request) {
        Long userId = request.getUserId();
        Long planId = request.getPlanId();
        return ResponseEntity.ok(userPlanSelectionService.selectPlan(userId, planId));
    }

    @GetMapping("/active/{userId}")
    public ResponseEntity<UserPlanSelectionDTO> getActivePlan(@PathVariable Long userId) {
        return ResponseEntity.ok(userPlanSelectionService.getActivePlan(userId));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<UserPlanSelectionDTO>> getPlanHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(userPlanSelectionService.getUserPlanHistory(userId));
    }

    // Get all available plans for users
    @GetMapping("/available")
    public ResponseEntity<?> getAvailablePlans() {
        try {
            List<Plan> plans = planRepository.findAll();

            List<PlanDTO> planDTOs = plans.stream()
                    .map(PlanMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(planDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to fetch plans");
        }
    }

    // Get plan details by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        return planRepository.findById(id)
                .map(plan -> ResponseEntity.ok(PlanMapper.toDTO(plan)))
                .orElse(ResponseEntity.notFound().build());
    }
}
