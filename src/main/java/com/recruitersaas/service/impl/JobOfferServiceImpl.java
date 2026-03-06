package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.JobOfferRequest;
import com.recruitersaas.dto.response.JobOfferResponse;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.mapper.JobOfferMapper;
import com.recruitersaas.model.JobOffer;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.repository.JobApplicationRepository;
import com.recruitersaas.repository.JobOfferRepository;
import com.recruitersaas.repository.RecruiterProfileRepository;
import com.recruitersaas.service.JobOfferService;
import com.recruitersaas.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobOfferMapper jobOfferMapper;
    private final SubscriptionService subscriptionService;

    @Override
    public JobOfferResponse createJobOffer(String recruiterEmail, JobOfferRequest request) {
        RecruiterProfile profile = getProfileByEmail(recruiterEmail);

        if (!subscriptionService.hasActiveSubscription(profile.getId())) {
            throw new BusinessException("Votre abonnement est inactif. Veuillez contacter l'administrateur.");
        }

        JobOffer offer = jobOfferMapper.toEntity(request);
        offer.setRecruiterProfile(profile);
        if (offer.getStatus() == null) offer.setStatus(JobOfferStatus.DRAFT);

        return toResponseWithCount(jobOfferRepository.save(offer));
    }

    @Override
    @Transactional(readOnly = true)
    public JobOfferResponse getJobOfferById(String id) {
        return toResponseWithCount(findById(id));
    }

    @Override
    public JobOfferResponse updateJobOffer(String id, JobOfferRequest request, String recruiterEmail) {
        JobOffer offer = findById(id);
        assertOwnership(offer, recruiterEmail);
        jobOfferMapper.updateEntity(request, offer);
        return toResponseWithCount(jobOfferRepository.save(offer));
    }

    @Override
    public void deleteJobOffer(String id, String recruiterEmail) {
        JobOffer offer = findById(id);
        assertOwnership(offer, recruiterEmail);
        jobOfferRepository.delete(offer);
    }

    @Override
    public void publishJobOffer(String id, String recruiterEmail) {
        JobOffer offer = findById(id);
        assertOwnership(offer, recruiterEmail);

        if (!subscriptionService.canCreateMoreOffers(offer.getRecruiterProfile().getId())) {
            throw new BusinessException("Vous avez atteint la limite d'offres publiées de votre plan.");
        }

        offer.setStatus(JobOfferStatus.PUBLISHED);
        offer.setPublishedAt(LocalDate.now());
        jobOfferRepository.save(offer);
    }

    @Override
    public void closeJobOffer(String id, String recruiterEmail) {
        JobOffer offer = findById(id);
        assertOwnership(offer, recruiterEmail);
        offer.setStatus(JobOfferStatus.CLOSED);
        jobOfferRepository.save(offer);
    }

    @Override
    public void archiveJobOffer(String id, String recruiterEmail) {
        JobOffer offer = findById(id);
        assertOwnership(offer, recruiterEmail);
        offer.setStatus(JobOfferStatus.ARCHIVED);
        jobOfferRepository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobOfferResponse> getRecruiterJobOffers(String recruiterEmail, JobOfferStatus status, int page, int size) {
        RecruiterProfile profile = getProfileByEmail(recruiterEmail);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<JobOffer> offers = status != null
                ? jobOfferRepository.findAllByRecruiterProfileIdAndStatus(profile.getId(), status, pageable)
                : jobOfferRepository.findAllByRecruiterProfileId(profile.getId(), pageable);

        return PageResponse.from(offers.map(this::toResponseWithCount));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobOfferResponse> getPublicJobOffers(String recruiterSlug, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<JobOffer> offers = jobOfferRepository.findPublishedByRecruiterSlug(recruiterSlug, pageable);
        return PageResponse.from(offers.map(this::toResponseWithCount));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobOfferResponse> getAllJobOffers(JobOfferStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobOffer> offers = status != null
                ? jobOfferRepository.findAll(pageable).map(o -> o) // filtre manuel si besoin
                : jobOfferRepository.findAll(pageable);
        return PageResponse.from(offers.map(this::toResponseWithCount));
    }

    // --- Helpers ---

    private JobOffer findById(String id) {
        return jobOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre introuvable: " + id));
    }

    private RecruiterProfile getProfileByEmail(String email) {
        return recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profil recruteur introuvable"));
    }

    private void assertOwnership(JobOffer offer, String recruiterEmail) {
        RecruiterProfile profile = getProfileByEmail(recruiterEmail);
        if (!offer.getRecruiterProfile().getId().equals(profile.getId())) {
            throw new BusinessException("Vous n'êtes pas autorisé à modifier cette offre");
        }
    }

    private JobOfferResponse toResponseWithCount(JobOffer offer) {
        JobOfferResponse response = jobOfferMapper.toResponse(offer);
        response.setApplicationCount(jobApplicationRepository.countByJobOfferId(offer.getId()));
        return response;
    }
}
