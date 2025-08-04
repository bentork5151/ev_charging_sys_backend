package com.bentork.ev_system.service;

import com.bentork.ev_system.dto.request.PlanDTO;
import com.bentork.ev_system.mapper.PlanMapper;
import com.bentork.ev_system.model.Admin;
import com.bentork.ev_system.model.Plan;
import com.bentork.ev_system.repository.AdminRepository;
import com.bentork.ev_system.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private AdminRepository adminRepository;

    public PlanDTO createPlan(PlanDTO dto, Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin not found"));

        Plan plan = PlanMapper.toEntity(dto);
        plan.setCreatedBy(admin);

        return PlanMapper.toDTO(planRepository.save(plan));
    }

    public List<PlanDTO> getAllPlans() {
        return planRepository.findAll()
                .stream()
                .map(PlanMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PlanDTO getPlanById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        return PlanMapper.toDTO(plan);
    }

    public PlanDTO updatePlan(Long id, PlanDTO dto) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        plan.setPlanName(dto.getPlanName());
        plan.setDescription(dto.getDescription());
        plan.setDurationMin(dto.getDurationMin());
        plan.setWalletDeduction(dto.getWalletDeduction());
        plan.setChargerType(dto.getChargerType());
        plan.setRate(dto.getRate());

        return PlanMapper.toDTO(planRepository.save(plan));
    }

    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new EntityNotFoundException("Plan not found");
        }
        planRepository.deleteById(id);
    }
}

