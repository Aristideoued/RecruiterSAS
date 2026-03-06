package com.recruitersaas.dto.response;

import com.recruitersaas.model.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionResponse {

    private String id;
    private PlanResponse plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate trialEndDate;
    private LocalDate nextBillingDate;
    private LocalDateTime createdAt;
}
