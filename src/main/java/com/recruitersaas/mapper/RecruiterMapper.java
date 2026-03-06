package com.recruitersaas.mapper;

import com.recruitersaas.dto.response.RecruiterResponse;
import com.recruitersaas.model.RecruiterProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {SubscriptionMapper.class})
public interface RecruiterMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.enabled", target = "enabled")
    @Mapping(source = "user.createdAt", target = "createdAt")
    RecruiterResponse toResponse(RecruiterProfile recruiterProfile);
}
