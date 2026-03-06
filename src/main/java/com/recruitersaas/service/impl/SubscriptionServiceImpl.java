package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.SubscriptionUpdateRequest;
import com.recruitersaas.dto.response.SubscriptionResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.mapper.SubscriptionMapper;
import com.recruitersaas.model.Plan;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.Subscription;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.model.enums.SubscriptionStatus;
import com.recruitersaas.repository.JobOfferRepository;
import com.recruitersaas.repository.PlanRepository;
import com.recruitersaas.repository.RecruiterProfileRepository;
import com.recruitersaas.repository.SubscriptionRepository;
import com.recruitersaas.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PlanRepository planRepository;
    private final JobOfferRepository jobOfferRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByRecruiterId(String recruiterProfileId) {
        Subscription subscription = subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable pour le recruteur: " + recruiterProfileId));
        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    public SubscriptionResponse updateSubscription(String recruiterProfileId, SubscriptionUpdateRequest request) {
        Subscription subscription = subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable pour le recruteur: " + recruiterProfileId));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan introuvable: " + request.getPlanId()));

        subscription.setPlan(plan);
        subscription.setStatus(request.getStatus());
        if (request.getEndDate() != null) subscription.setEndDate(request.getEndDate());
        if (request.getTrialEndDate() != null) subscription.setTrialEndDate(request.getTrialEndDate());

        return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
    }

    @Override
    public void suspendSubscription(String recruiterProfileId) {
        Subscription subscription = subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable"));
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscriptionRepository.save(subscription);

        // Désactiver le compte utilisateur
        RecruiterProfile profile = recruiterProfileRepository.findById(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruteur introuvable"));
        profile.getUser().setEnabled(false);
        recruiterProfileRepository.save(profile);
    }

    @Override
    public void reactivateSubscription(String recruiterProfileId) {
        Subscription subscription = subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Abonnement introuvable"));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        // Réactiver le compte utilisateur
        RecruiterProfile profile = recruiterProfileRepository.findById(recruiterProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruteur introuvable"));
        profile.getUser().setEnabled(true);
        recruiterProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String recruiterProfileId) {
        return subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .map(s -> s.getStatus() == SubscriptionStatus.ACTIVE || s.getStatus() == SubscriptionStatus.TRIAL)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateMoreOffers(String recruiterProfileId) {
        Subscription subscription = subscriptionRepository.findByRecruiterProfileId(recruiterProfileId)
                .orElse(null);
        if (subscription == null) return false;

        int maxOffers = subscription.getPlan().getMaxJobOffers();
        if (maxOffers == -1) return true; // illimité

        long currentOffers = jobOfferRepository.countByRecruiterProfileIdAndStatus(
                recruiterProfileId, JobOfferStatus.PUBLISHED);
        return currentOffers < maxOffers;
    }
}
