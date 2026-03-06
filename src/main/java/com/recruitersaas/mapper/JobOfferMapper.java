package com.recruitersaas.mapper;

import com.recruitersaas.dto.request.JobOfferRequest;
import com.recruitersaas.dto.response.JobOfferResponse;
import com.recruitersaas.model.JobOffer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobOfferMapper {

    @Mapping(source = "recruiterProfile.id", target = "recruiterProfileId")
    @Mapping(source = "recruiterProfile.companyName", target = "recruiterCompanyName")
    @Mapping(source = "recruiterProfile.companyLogo", target = "recruiterCompanyLogo")
    @Mapping(source = "recruiterProfile.slug", target = "recruiterSlug")
    @Mapping(target = "applicationCount", ignore = true)
    JobOfferResponse toResponse(JobOffer jobOffer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recruiterProfile", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobOffer toEntity(JobOfferRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recruiterProfile", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(JobOfferRequest request, @MappingTarget JobOffer jobOffer);
}
