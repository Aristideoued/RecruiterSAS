package com.recruitersaas.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanRequest {

    @NotBlank(message = "Le nom du plan est obligatoire")
    private String name;

    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    private BigDecimal monthlyPrice;

    private int maxJobOffers = 5;
    private int maxApplicationsPerOffer = 50;
    private boolean cvParsingEnabled = false;
    private boolean analyticsEnabled = false;
    private boolean customBrandingEnabled = false;
}
