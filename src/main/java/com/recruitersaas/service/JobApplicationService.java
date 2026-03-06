package com.recruitersaas.service;

import com.recruitersaas.dto.request.ApplicationStatusUpdateRequest;
import com.recruitersaas.dto.request.JobApplicationRequest;
import com.recruitersaas.dto.response.JobApplicationResponse;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.model.enums.ApplicationStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JobApplicationService {

    JobApplicationResponse submitApplication(String jobOfferId, JobApplicationRequest request, List<MultipartFile> files);

    JobApplicationResponse getApplicationById(String id, String recruiterEmail);

    JobApplicationResponse updateApplicationStatus(String id, ApplicationStatusUpdateRequest request, String recruiterEmail);

    void deleteApplication(String id, String recruiterEmail);

    PageResponse<JobApplicationResponse> getApplicationsByJobOffer(String jobOfferId, ApplicationStatus status, int page, int size, String recruiterEmail);

    PageResponse<JobApplicationResponse> getAllRecruiterApplications(String recruiterEmail, ApplicationStatus status, int page, int size);

    PageResponse<JobApplicationResponse> getAllApplications(int page, int size);

    PageResponse<JobApplicationResponse> getApplicationsByJobOfferSortedByScore(String jobOfferId, ApplicationStatus status, int page, int size, String recruiterEmail);

    PageResponse<JobApplicationResponse> getAllRecruiterApplicationsSortedByScore(String recruiterEmail, ApplicationStatus status, int page, int size);

    void triggerScoring(String applicationId, String recruiterEmail);
}
