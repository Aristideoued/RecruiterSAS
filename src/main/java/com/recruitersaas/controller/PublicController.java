package com.recruitersaas.controller;

import com.recruitersaas.dto.request.JobApplicationRequest;
import com.recruitersaas.dto.response.*;
import com.recruitersaas.service.JobApplicationService;
import com.recruitersaas.service.JobOfferService;
import com.recruitersaas.service.PlanService;
import com.recruitersaas.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final JobOfferService jobOfferService;
    private final JobApplicationService jobApplicationService;
    private final RecruiterService recruiterService;
    private final PlanService planService;

    // Récupérer les offres publiées d'un recruteur (page publique de candidature)
    @GetMapping("/recruiters/{slug}/offers")
    public ResponseEntity<PageResponse<JobOfferResponse>> getPublicOffers(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobOfferService.getPublicJobOffers(slug, page, size));
    }

    // Détail d'une offre publique
    @GetMapping("/offers/{offerId}")
    public ResponseEntity<JobOfferResponse> getPublicOffer(@PathVariable String offerId) {
        return ResponseEntity.ok(jobOfferService.getJobOfferById(offerId));
    }

    // Postuler à une offre (multipart: JSON data + fichiers PDF)
    @PostMapping("/offers/{offerId}/apply")
    public ResponseEntity<JobApplicationResponse> apply(
            @PathVariable String offerId,
            @RequestPart("data") @Valid JobApplicationRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobApplicationService.submitApplication(offerId, request, files));
    }

    // Plans disponibles (pour affichage tarification)
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getActivePlans() {
        return ResponseEntity.ok(planService.getAllPlans(true));
    }

    // Infos publiques d'un recruteur (nom entreprise, logo, slug)
    @GetMapping("/recruiters/{slug}")
    public ResponseEntity<RecruiterResponse> getRecruiterPublicInfo(@PathVariable String slug) {
        return ResponseEntity.ok(recruiterService.getRecruiterBySlug(slug));
    }
}
