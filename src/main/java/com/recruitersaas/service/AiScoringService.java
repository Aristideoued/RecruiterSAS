package com.recruitersaas.service;

import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.JobOffer;

public interface AiScoringService {

    /**
     * Calcule le score IA de façon asynchrone et le persiste.
     */
    void scoreApplicationAsync(JobApplication application, JobOffer jobOffer);

    /**
     * Calcule et persiste le score IA de façon synchrone.
     */
    void scoreApplication(JobApplication application, JobOffer jobOffer);
}
