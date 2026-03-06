package com.recruitersaas.service;

import com.recruitersaas.model.enums.ApplicationStatus;

public interface EmailService {

    /**
     * Notifie le recruteur qu'une nouvelle candidature a été reçue.
     * Tous les paramètres sont des Strings : aucune entité JPA passée (évite LazyInitializationException en @Async).
     */
    void sendNewApplicationNotification(
            String recruiterEmail,
            String candidateName,
            String candidateEmail,
            String candidatePhone,
            String offerTitle,
            String companyName
    );

    /**
     * Notifie le candidat que le statut de sa candidature a changé.
     */
    void sendStatusChangeNotification(
            String candidateEmail,
            String candidateFirstName,
            String offerTitle,
            String companyName,
            ApplicationStatus newStatus
    );
}
