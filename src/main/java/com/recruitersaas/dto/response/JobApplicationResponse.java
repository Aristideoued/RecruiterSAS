package com.recruitersaas.dto.response;

import com.recruitersaas.model.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class JobApplicationResponse {

    private String id;
    private String candidateFirstName;
    private String candidateLastName;
    private String candidateEmail;
    private String candidatePhone;
    private String candidateLinkedin;
    private String coverLetterText;
    private String recruiterNotes;
    private ApplicationStatus status;
    private Integer rating;
    private Double aiScore;
    private String aiScoreSummary;
    private List<ApplicationFileResponse> files;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;

    // Infos de l'offre associée
    private String jobOfferId;
    private String jobOfferTitle;
}
