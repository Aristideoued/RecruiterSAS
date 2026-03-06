package com.recruitersaas.dto.response;

import com.recruitersaas.model.enums.JobOfferStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class JobOfferResponse {

    private String id;
    private String title;
    private String description;
    private String requirements;
    private String location;
    private String contractType;
    private String workMode;
    private String salaryRange;
    private String experienceLevel;
    private String category;
    private JobOfferStatus status;
    private long applicationCount;
    private LocalDate publishedAt;
    private LocalDate closingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Infos recruteur (vue publique candidat)
    private String recruiterProfileId;
    private String recruiterCompanyName;
    private String recruiterCompanyLogo;
    private String recruiterSlug;
}
