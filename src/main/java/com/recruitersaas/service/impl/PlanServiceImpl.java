package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.PlanRequest;
import com.recruitersaas.dto.response.PlanResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.mapper.PlanMapper;
import com.recruitersaas.model.Plan;
import com.recruitersaas.repository.PlanRepository;
import com.recruitersaas.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Override
    public PlanResponse createPlan(PlanRequest request) {
        if (planRepository.existsByName(request.getName())) {
            throw new BusinessException("Un plan avec ce nom existe déjà: " + request.getName());
        }
        Plan plan = planMapper.toEntity(request);
        plan.setActive(true);
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans(boolean onlyActive) {
        List<Plan> plans = onlyActive
                ? planRepository.findAllByActiveTrue()
                : planRepository.findAll();
        return plans.stream().map(planMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanResponse getPlanById(String id) {
        return planMapper.toResponse(findPlanById(id));
    }

    @Override
    public PlanResponse updatePlan(String id, PlanRequest request) {
        Plan plan = findPlanById(id);
        planMapper.updateEntity(request, plan);
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Override
    public void togglePlanStatus(String id) {
        Plan plan = findPlanById(id);
        plan.setActive(!plan.isActive());
        planRepository.save(plan);
    }

    private Plan findPlanById(String id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan introuvable: " + id));
    }
}
