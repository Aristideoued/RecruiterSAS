package com.recruitersaas.service;

import com.recruitersaas.dto.request.RegisterRecruiterRequest;
import com.recruitersaas.dto.request.UpdateRecruiterRequest;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.dto.response.RecruiterResponse;

public interface RecruiterService {

    RecruiterResponse createRecruiter(RegisterRecruiterRequest request);

    RecruiterResponse getRecruiterById(String recruiterProfileId);

    RecruiterResponse getRecruiterBySlug(String slug);

    RecruiterResponse getMyProfile(String email);

    RecruiterResponse updateRecruiter(String recruiterProfileId, UpdateRecruiterRequest request);

    void toggleRecruiterStatus(String recruiterProfileId);

    void deleteRecruiter(String recruiterProfileId);

    PageResponse<RecruiterResponse> getAllRecruiters(int page, int size);
}
