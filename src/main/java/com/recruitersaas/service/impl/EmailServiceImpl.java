package com.recruitersaas.service.impl;

import com.recruitersaas.model.enums.ApplicationStatus;
import com.recruitersaas.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implémentation du service email.
 * Les méthodes sont @Async → exécutées dans un thread séparé.
 * IMPORTANT : ne jamais passer d'entités JPA ici (session Hibernate fermée).
 *             Tous les paramètres sont des types primitifs/String.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Nouvelle candidature → recruteur ────────────────────────────────────

    @Async
    @Override
    public void sendNewApplicationNotification(
            String recruiterEmail,
            String candidateName,
            String candidateEmail,
            String candidatePhone,
            String offerTitle,
            String companyName) {

        String subject = "📩 Nouvelle candidature pour « " + offerTitle + " »";
        String body = buildNewApplicationHtml(
                candidateName, candidateEmail, candidatePhone, offerTitle, companyName);
        sendHtml(recruiterEmail, subject, body);
    }

    // ─── Changement de statut → candidat ─────────────────────────────────────

    @Async
    @Override
    public void sendStatusChangeNotification(
            String candidateEmail,
            String candidateFirstName,
            String offerTitle,
            String companyName,
            ApplicationStatus newStatus) {

        StatusMessage msg = resolveMessage(newStatus);
        String subject = msg.emoji() + " " + msg.subject() + " — " + offerTitle;
        String body = buildStatusChangeHtml(
                candidateFirstName, offerTitle, companyName, newStatus,
                msg.subject(), msg.paragraph(), msg.emoji());
        sendHtml(candidateEmail, subject, body);
    }

    // ─── Templates HTML ───────────────────────────────────────────────────────

    private String buildNewApplicationHtml(String candidateName, String candidateEmail,
                                           String candidatePhone, String offerTitle,
                                           String companyName) {
        String phoneRow = (candidatePhone != null && !candidatePhone.isBlank())
                ? "<tr><td style='padding:6px 0;color:#9ca3af;font-size:13px'>Téléphone</td>"
                  + "<td style='padding:6px 0;color:#1e293b'>" + candidatePhone + "</td></tr>"
                : "";

        return htmlWrapper(
                "Nouvelle candidature reçue",
                "<h2 style='color:#1a237e;margin:0 0 8px'>Nouvelle candidature reçue !</h2>"
                + "<p style='color:#6b7280;margin:0 0 24px'>Une nouvelle candidature vient d'être soumise sur votre offre.</p>"
                + "<div style='background:#f8fafc;border-radius:12px;padding:20px;margin-bottom:24px'>"
                + "  <table style='width:100%;border-collapse:collapse'>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px;width:140px'>Offre</td>"
                + "        <td style='padding:6px 0;font-weight:600;color:#1e293b'>" + offerTitle + "</td></tr>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px'>Candidat</td>"
                + "        <td style='padding:6px 0;font-weight:600;color:#1e293b'>" + candidateName + "</td></tr>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px'>Email</td>"
                + "        <td style='padding:6px 0;color:#6366f1'>" + candidateEmail + "</td></tr>"
                + phoneRow
                + "  </table>"
                + "</div>"
                + "<a href='" + frontendUrl + "/recruiter/applications'"
                + "   style='display:inline-block;background:#3949ab;color:#fff;text-decoration:none;"
                + "          padding:12px 28px;border-radius:8px;font-weight:600;font-size:15px'>"
                + "  Voir la candidature →"
                + "</a>",
                companyName
        );
    }

    private String buildStatusChangeHtml(String candidateName, String offerTitle,
                                         String companyName, ApplicationStatus status,
                                         String headline, String paragraph, String emoji) {
        String badgeColor = switch (status) {
            case SHORTLISTED -> "#7e22ce";
            case HIRED       -> "#15803d";
            case REJECTED    -> "#b91c1c";
            case REVIEWED    -> "#1d4ed8";
            default          -> "#c2410c";
        };
        String badgeBg = switch (status) {
            case SHORTLISTED -> "#faf5ff";
            case HIRED       -> "#f0fdf4";
            case REJECTED    -> "#fef2f2";
            case REVIEWED    -> "#eff6ff";
            default          -> "#fff7ed";
        };

        return htmlWrapper(
                headline,
                "<h2 style='color:#1a237e;margin:0 0 8px'>" + emoji + " " + headline + "</h2>"
                + "<p style='color:#6b7280;margin:0 0 20px'>Bonjour <strong>" + candidateName + "</strong>,</p>"
                + "<p style='color:#374151;line-height:1.6;margin:0 0 24px'>" + paragraph + "</p>"
                + "<div style='background:#f8fafc;border-radius:12px;padding:20px;margin-bottom:24px'>"
                + "  <table style='width:100%;border-collapse:collapse'>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px;width:140px'>Offre</td>"
                + "        <td style='padding:6px 0;font-weight:600;color:#1e293b'>" + offerTitle + "</td></tr>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px'>Entreprise</td>"
                + "        <td style='padding:6px 0;color:#1e293b'>" + companyName + "</td></tr>"
                + "    <tr><td style='padding:6px 0;color:#9ca3af;font-size:13px'>Statut</td>"
                + "        <td style='padding:6px 0'>"
                + "          <span style='background:" + badgeBg + ";color:" + badgeColor + ";"
                + "                       padding:3px 12px;border-radius:20px;font-size:12px;font-weight:700'>"
                +              statusLabel(status)
                + "          </span></td></tr>"
                + "  </table>"
                + "</div>"
                + "<p style='color:#9ca3af;font-size:13px;margin:0'>Bonne continuation dans votre recherche d'emploi.</p>",
                companyName
        );
    }

    private String htmlWrapper(String title, String content, String companyName) {
        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"><title>%s</title></head>
            <body style="margin:0;padding:0;background:#f0f2f5;font-family:'Helvetica Neue',Arial,sans-serif">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:40px 16px">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#fff;border-radius:16px;overflow:hidden;
                                box-shadow:0 4px 24px rgba(0,0,0,.08)">
                    <tr><td style="background:linear-gradient(135deg,#1a237e,#3949ab);
                                   padding:32px 40px;text-align:center">
                      <h1 style="color:#fff;margin:0;font-size:22px;font-weight:800;
                                 letter-spacing:-0.3px">RecruiterSaaS</h1>
                      <p style="color:#c7d2fe;margin:6px 0 0;font-size:13px">Plateforme de recrutement</p>
                    </td></tr>
                    <tr><td style="padding:40px">%s</td></tr>
                    <tr><td style="background:#f8fafc;padding:24px 40px;text-align:center;
                                   border-top:1px solid #e5e7eb">
                      <p style="color:#9ca3af;font-size:12px;margin:0">
                        Cet email a été envoyé par la plateforme RecruiterSaaS
                        pour le compte de <strong>%s</strong>.<br>
                        © 2024 RecruiterSaaS — Tous droits réservés
                      </p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(title, content, companyName);
    }

    // ─── Messages par statut ──────────────────────────────────────────────────

    private StatusMessage resolveMessage(ApplicationStatus status) {
        return switch (status) {
            case REVIEWED -> new StatusMessage(
                    "🔍", "Votre candidature a été examinée",
                    "Bonne nouvelle, votre candidature a été lue ! Le recruteur est en cours d'évaluation "
                    + "des profils reçus. Nous vous tiendrons informé(e) des prochaines étapes dès que possible."
            );
            case SHORTLISTED -> new StatusMessage(
                    "⭐", "Vous avez été présélectionné(e) !",
                    "Excellente nouvelle ! Votre profil a retenu l'attention du recruteur et vous faites partie "
                    + "des candidats présélectionnés. Vous serez prochainement contacté(e) pour la suite du processus."
            );
            case REJECTED -> new StatusMessage(
                    "📋", "Suite à votre candidature",
                    "Nous vous remercions pour l'intérêt que vous avez porté à ce poste. Après examen attentif, "
                    + "votre profil ne correspond pas aux critères recherchés. "
                    + "Nous vous encourageons à postuler à d'autres opportunités sur notre plateforme."
            );
            case HIRED -> new StatusMessage(
                    "🎉", "Félicitations, votre candidature a été retenue !",
                    "C'est avec grand plaisir que nous vous informons que votre candidature a été retenue ! "
                    + "Le recruteur va prendre contact avec vous très prochainement pour les modalités de votre intégration. "
                    + "Félicitations et bienvenue dans cette nouvelle aventure professionnelle !"
            );
            default -> new StatusMessage(
                    "📬", "Mise à jour de votre candidature",
                    "Le statut de votre candidature a été mis à jour. Le recruteur reviendra vers vous prochainement."
            );
        };
    }

    // ─── Envoi effectif ───────────────────────────────────────────────────────

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email envoyé à {} : {}", to, subject);
        } catch (MessagingException e) {
            log.error("Échec envoi email à {} : {}", to, e.getMessage());
        }
    }

    private String statusLabel(ApplicationStatus status) {
        return switch (status) {
            case PENDING     -> "En attente";
            case REVIEWED    -> "Examinée";
            case SHORTLISTED -> "Présélectionnée";
            case REJECTED    -> "Rejetée";
            case HIRED       -> "Recrutée";
        };
    }

    private record StatusMessage(String emoji, String subject, String paragraph) {}
}
