package com.recruitersaas.service.impl;

import com.recruitersaas.dto.response.DashboardStatsResponse;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.enums.ApplicationStatus;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.model.enums.SubscriptionStatus;
import com.recruitersaas.repository.*;
import com.recruitersaas.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobOfferRepository jobOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public DashboardStatsResponse getRecruiterStats(String recruiterEmail) {
        RecruiterProfile profile = recruiterProfileRepository.findByUserEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Profil recruteur introuvable"));

        String profileId = profile.getId();

        return DashboardStatsResponse.builder()
                .totalJobOffers(jobOfferRepository.countByRecruiterProfileId(profileId))
                .publishedJobOffers(jobOfferRepository.countByRecruiterProfileIdAndStatus(profileId, JobOfferStatus.PUBLISHED))
                .draftJobOffers(jobOfferRepository.countByRecruiterProfileIdAndStatus(profileId, JobOfferStatus.DRAFT))
                .closedJobOffers(jobOfferRepository.countByRecruiterProfileIdAndStatus(profileId, JobOfferStatus.CLOSED))
                .totalApplications(jobApplicationRepository.countByRecruiterProfileId(profileId))
                .pendingApplications(jobApplicationRepository.countPendingByRecruiterProfileId(profileId))
                .build();
    }

    @Override
    public DashboardStatsResponse getSuperAdminStats() {
        long totalRecruiters = recruiterProfileRepository.count();
        long activeRecruiters = recruiterProfileRepository.findAllByUserEnabled(true,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

        return DashboardStatsResponse.builder()
                .totalRecruiters(totalRecruiters)
                .activeRecruiters(activeRecruiters)
                .suspendedRecruiters(totalRecruiters - activeRecruiters)
                .totalPlans(planRepository.count())
                .totalSubscriptions(subscriptionRepository.count())
                .activeSubscriptions(subscriptionRepository.findAllByStatus(SubscriptionStatus.ACTIVE).size()
                        + subscriptionRepository.findAllByStatus(SubscriptionStatus.TRIAL).size())
                .build();
    }
}
