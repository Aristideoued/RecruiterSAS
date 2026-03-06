package com.recruitersaas.mapper;

import com.recruitersaas.dto.request.JobApplicationRequest;
import com.recruitersaas.dto.response.JobApplicationResponse;
import com.recruitersaas.model.JobApplication;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {ApplicationFileMapper.class})
public interface JobApplicationMapper {

    @Mapping(source = "jobOffer.id", target = "jobOfferId")
    @Mapping(source = "jobOffer.title", target = "jobOfferTitle")
    @Mapping(source = "files", target = "files")
    JobApplicationResponse toResponse(JobApplication jobApplication);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobOffer", ignore = true)
    @Mapping(target = "recruiterNotes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    JobApplication toEntity(JobApplicationRequest request);
}
