package com.recruitersaas.service;

import com.recruitersaas.dto.request.SubscriptionUpdateRequest;
import com.recruitersaas.dto.response.SubscriptionResponse;

public interface SubscriptionService {

    SubscriptionResponse getSubscriptionByRecruiterId(String recruiterProfileId);

    SubscriptionResponse updateSubscription(String recruiterProfileId, SubscriptionUpdateRequest request);

    void suspendSubscription(String recruiterProfileId);

    void reactivateSubscription(String recruiterProfileId);

    boolean hasActiveSubscription(String recruiterProfileId);

    boolean canCreateMoreOffers(String recruiterProfileId);
}
