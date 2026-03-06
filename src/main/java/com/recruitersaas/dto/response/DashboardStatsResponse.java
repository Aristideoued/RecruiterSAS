package com.recruitersaas.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {

    // Statistiques recruteur
    private long totalJobOffers;
    private long publishedJobOffers;
    private long draftJobOffers;
    private long closedJobOffers;
    private long totalApplications;
    private long pendingApplications;
    private long shortlistedApplications;
    private long hiredApplications;

    // Statistiques super admin
    private long totalRecruiters;
    private long activeRecruiters;
    private long suspendedRecruiters;
    private long totalPlans;
    private long totalSubscriptions;
    private long activeSubscriptions;
}
