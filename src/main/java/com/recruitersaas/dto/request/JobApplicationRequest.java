package com.recruitersaas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobApplicationRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String candidateFirstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String candidateLastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String candidateEmail;

    private String candidatePhone;
    private String candidateLinkedin;
    private String coverLetterText;
}
