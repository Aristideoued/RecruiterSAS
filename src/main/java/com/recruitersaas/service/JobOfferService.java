package com.recruitersaas.service;

import com.recruitersaas.dto.request.JobOfferRequest;
import com.recruitersaas.dto.response.JobOfferResponse;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.model.enums.JobOfferStatus;

public interface JobOfferService {

    JobOfferResponse createJobOffer(String recruiterEmail, JobOfferRequest request);

    JobOfferResponse getJobOfferById(String id);

    JobOfferResponse updateJobOffer(String id, JobOfferRequest request, String recruiterEmail);

    void deleteJobOffer(String id, String recruiterEmail);

    void publishJobOffer(String id, String recruiterEmail);

    void closeJobOffer(String id, String recruiterEmail);

    void archiveJobOffer(String id, String recruiterEmail);

    void unarchiveJobOffer(String id, String recruiterEmail);

    PageResponse<JobOfferResponse> getRecruiterJobOffers(String recruiterEmail, JobOfferStatus status, int page, int size);

    PageResponse<JobOfferResponse> getPublicJobOffers(String recruiterSlug, int page, int size);

    PageResponse<JobOfferResponse> getAllJobOffers(JobOfferStatus status, int page, int size);
}
