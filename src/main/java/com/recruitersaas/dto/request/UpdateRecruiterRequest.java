package com.recruitersaas.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateRecruiterRequest {

    @Email(message = "Format d'email invalide")
    private String email;

    private String firstName;
    private String lastName;
    private String companyName;
    private String companyWebsite;
    private String phone;
    private String address;
    private String siret;
}
