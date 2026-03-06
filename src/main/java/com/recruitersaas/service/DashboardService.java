package com.recruitersaas.service;

import com.recruitersaas.dto.response.DashboardStatsResponse;

public interface DashboardService {

    DashboardStatsResponse getRecruiterStats(String recruiterEmail);

    DashboardStatsResponse getSuperAdminStats();
}
