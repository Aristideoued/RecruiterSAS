package com.recruitersaas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecruiterResponse {

    private String id;           // recruiterProfile id
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String companyName;
    private String companyLogo;
    private String companyWebsite;
    private String phone;
    private String address;
    private String siret;
    private String slug;
    private boolean enabled;
    private SubscriptionResponse subscription;
    private LocalDateTime createdAt;
}
