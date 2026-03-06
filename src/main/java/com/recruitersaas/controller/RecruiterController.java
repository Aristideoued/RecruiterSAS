package com.recruitersaas.controller;

import com.recruitersaas.dto.request.ApplicationStatusUpdateRequest;
import com.recruitersaas.dto.request.JobOfferRequest;
import com.recruitersaas.dto.response.*;
import com.recruitersaas.model.enums.ApplicationStatus;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.service.DashboardService;
import com.recruitersaas.service.JobApplicationService;
import com.recruitersaas.service.JobOfferService;
import com.recruitersaas.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final JobOfferService jobOfferService;
    private final JobApplicationService jobApplicationService;
    private final DashboardService dashboardService;

    // ==================== PROFIL ====================

    @GetMapping("/profile")
    public ResponseEntity<RecruiterResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(recruiterService.getMyProfile(userDetails.getUsername()));
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getRecruiterStats(userDetails.getUsername()));
    }

    // ==================== OFFRES ====================

    @PostMapping("/job-offers")
    public ResponseEntity<JobOfferResponse> createJobOffer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobOfferService.createJobOffer(userDetails.getUsername(), request));
    }

    @GetMapping("/job-offers")
    public ResponseEntity<PageResponse<JobOfferResponse>> getMyJobOffers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) JobOfferStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getRecruiterJobOffers(userDetails.getUsername(), status, page, size));
    }

    @GetMapping("/job-offers/{id}")
    public ResponseEntity<JobOfferResponse> getJobOffer(@PathVariable String id) {
        return ResponseEntity.ok(jobOfferService.getJobOfferById(id));
    }

    @PutMapping("/job-offers/{id}")
    public ResponseEntity<JobOfferResponse> updateJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody JobOfferRequest request) {
        return ResponseEntity.ok(jobOfferService.updateJobOffer(id, request, userDetails.getUsername()));
    }

    @PatchMapping("/job-offers/{id}/publish")
    public ResponseEntity<Void> publishJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobOfferService.publishJobOffer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/job-offers/{id}/close")
    public ResponseEntity<Void> closeJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobOfferService.closeJobOffer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/job-offers/{id}/archive")
    public ResponseEntity<Void> archiveJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobOfferService.archiveJobOffer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/job-offers/{id}/unarchive")
    public ResponseEntity<Void> unarchiveJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobOfferService.unarchiveJobOffer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/job-offers/{id}")
    public ResponseEntity<Void> deleteJobOffer(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobOfferService.deleteJobOffer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ==================== CANDIDATURES ====================

    @GetMapping("/job-offers/{offerId}/applications")
    public ResponseEntity<PageResponse<JobApplicationResponse>> getApplicationsByOffer(
            @PathVariable String offerId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if ("score".equalsIgnoreCase(sortBy)) {
            return ResponseEntity.ok(jobApplicationService.getApplicationsByJobOfferSortedByScore(
                    offerId, status, page, size, userDetails.getUsername()));
        }
        return ResponseEntity.ok(jobApplicationService.getApplicationsByJobOffer(
                offerId, status, page, size, userDetails.getUsername()));
    }

    @GetMapping("/applications")
    public ResponseEntity<PageResponse<JobApplicationResponse>> getAllMyApplications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if ("score".equalsIgnoreCase(sortBy)) {
            return ResponseEntity.ok(jobApplicationService.getAllRecruiterApplicationsSortedByScore(
                    userDetails.getUsername(), status, page, size));
        }
        return ResponseEntity.ok(jobApplicationService.getAllRecruiterApplications(
                userDetails.getUsername(), status, page, size));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<JobApplicationResponse> getApplication(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(jobApplicationService.getApplicationById(id, userDetails.getUsername()));
    }

    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateApplicationStatus(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ApplicationStatusUpdateRequest request) {
        return ResponseEntity.ok(jobApplicationService.updateApplicationStatus(id, request, userDetails.getUsername()));
    }

    @PostMapping("/applications/{id}/score")
    public ResponseEntity<Void> triggerScoring(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobApplicationService.triggerScoring(id, userDetails.getUsername());
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        jobApplicationService.deleteApplication(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
