package com.recruitersaas.service;

import com.recruitersaas.dto.request.PlanRequest;
import com.recruitersaas.dto.response.PlanResponse;

import java.util.List;

public interface PlanService {

    PlanResponse createPlan(PlanRequest request);

    List<PlanResponse> getAllPlans(boolean onlyActive);

    PlanResponse getPlanById(String id);

    PlanResponse updatePlan(String id, PlanRequest request);

    void togglePlanStatus(String id);
}
