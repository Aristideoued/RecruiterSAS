package com.recruitersaas.dto.request;

import com.recruitersaas.model.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionUpdateRequest {

    @NotBlank(message = "Le planId est obligatoire")
    private String planId;

    @NotNull(message = "Le statut est obligatoire")
    private SubscriptionStatus status;

    private LocalDate endDate;
    private LocalDate trialEndDate;
}
