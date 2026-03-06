package com.recruitersaas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PlanResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private int maxJobOffers;
    private int maxApplicationsPerOffer;
    private boolean cvParsingEnabled;
    private boolean analyticsEnabled;
    private boolean customBrandingEnabled;
    private boolean active;
}
