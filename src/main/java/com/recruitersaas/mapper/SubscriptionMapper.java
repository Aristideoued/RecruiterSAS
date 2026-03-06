package com.recruitersaas.mapper;

import com.recruitersaas.dto.response.SubscriptionResponse;
import com.recruitersaas.model.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PlanMapper.class})
public interface SubscriptionMapper {

    SubscriptionResponse toResponse(Subscription subscription);
}
