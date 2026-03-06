package com.recruitersaas.dto.request;

import com.recruitersaas.model.enums.ApplicationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private ApplicationStatus status;

    private String recruiterNotes;

    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    private Integer rating;
}
