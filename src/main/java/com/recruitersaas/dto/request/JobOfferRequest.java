package com.recruitersaas.dto.request;

import com.recruitersaas.model.enums.JobOfferStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class JobOfferRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String requirements;
    private String location;
    private String contractType;
    private String workMode;
    private String salaryRange;
    private String experienceLevel;
    private String category;
    private JobOfferStatus status;
    private LocalDate closingDate;
}
