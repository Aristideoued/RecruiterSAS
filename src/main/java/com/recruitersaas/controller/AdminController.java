package com.recruitersaas.controller;

import com.recruitersaas.dto.request.PlanRequest;
import com.recruitersaas.dto.request.RegisterRecruiterRequest;
import com.recruitersaas.dto.request.SubscriptionUpdateRequest;
import com.recruitersaas.dto.request.UpdateRecruiterRequest;
import com.recruitersaas.dto.response.*;
import com.recruitersaas.model.enums.ApplicationStatus;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final RecruiterService recruiterService;
    private final PlanService planService;
    private final SubscriptionService subscriptionService;
    private final DashboardService dashboardService;
    private final JobOfferService jobOfferService;
    private final JobApplicationService jobApplicationService;

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getSuperAdminStats());
    }

    // ==================== RECRUTEURS ====================

    @PostMapping("/recruiters")
    public ResponseEntity<RecruiterResponse> createRecruiter(
            @Valid @RequestBody RegisterRecruiterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recruiterService.createRecruiter(request));
    }

    @GetMapping("/recruiters")
    public ResponseEntity<PageResponse<RecruiterResponse>> getAllRecruiters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(recruiterService.getAllRecruiters(page, size));
    }

    @GetMapping("/recruiters/{id}")
    public ResponseEntity<RecruiterResponse> getRecruiter(@PathVariable String id) {
        return ResponseEntity.ok(recruiterService.getRecruiterById(id));
    }

    @PutMapping("/recruiters/{id}")
    public ResponseEntity<RecruiterResponse> updateRecruiter(
            @PathVariable String id,
            @Valid @RequestBody UpdateRecruiterRequest request) {
        return ResponseEntity.ok(recruiterService.updateRecruiter(id, request));
    }

    @PatchMapping("/recruiters/{id}/toggle")
    public ResponseEntity<Void> toggleRecruiterStatus(@PathVariable String id) {
        recruiterService.toggleRecruiterStatus(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/recruiters/{id}")
    public ResponseEntity<Void> deleteRecruiter(@PathVariable String id) {
        recruiterService.deleteRecruiter(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== PLANS ====================

    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.createPlan(request));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getAllPlans(
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(planService.getAllPlans(onlyActive));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<PlanResponse> getPlan(@PathVariable String id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanResponse> updatePlan(
            @PathVariable String id,
            @Valid @RequestBody PlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @PatchMapping("/plans/{id}/toggle")
    public ResponseEntity<Void> togglePlanStatus(@PathVariable String id) {
        planService.togglePlanStatus(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ABONNEMENTS ====================

    @GetMapping("/recruiters/{recruiterId}/subscription")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable String recruiterId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByRecruiterId(recruiterId));
    }

    @PutMapping("/recruiters/{recruiterId}/subscription")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable String recruiterId,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(recruiterId, request));
    }

    @PatchMapping("/recruiters/{recruiterId}/subscription/suspend")
    public ResponseEntity<Void> suspendSubscription(@PathVariable String recruiterId) {
        subscriptionService.suspendSubscription(recruiterId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/recruiters/{recruiterId}/subscription/reactivate")
    public ResponseEntity<Void> reactivateSubscription(@PathVariable String recruiterId) {
        subscriptionService.reactivateSubscription(recruiterId);
        return ResponseEntity.noContent().build();
    }

    // ==================== VUES GLOBALES ====================

    @GetMapping("/job-offers")
    public ResponseEntity<PageResponse<JobOfferResponse>> getAllJobOffers(
            @RequestParam(required = false) JobOfferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(jobOfferService.getAllJobOffers(status, page, size));
    }

    @GetMapping("/applications")
    public ResponseEntity<PageResponse<JobApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(jobApplicationService.getAllApplications(page, size));
    }
}
