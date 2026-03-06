package com.recruitersaas.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitersaas.model.ApplicationFile;
import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.JobOffer;
import com.recruitersaas.repository.JobApplicationRepository;
import com.recruitersaas.service.AiScoringService;
import com.recruitersaas.service.FileTextExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiScoringServiceImpl implements AiScoringService {

    private final JobApplicationRepository jobApplicationRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final FileTextExtractorService fileTextExtractor;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:claude-sonnet-4-6}")
    private String model;

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    @Override
    @Async
    public void scoreApplicationAsync(JobApplication application, JobOffer jobOffer) {
        scoreApplication(application, jobOffer);
    }

    @Override
    public void scoreApplication(JobApplication application, JobOffer jobOffer) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Clé API Claude non configurée — scoring IA désactivé pour la candidature {}", application.getId());
            return;
        }

        try {
            String prompt = buildScoringPrompt(application, jobOffer);
            String responseText = callClaudeApi(prompt);
            parseScoringResponse(responseText, application);
        } catch (Exception e) {
            log.error("Erreur lors du scoring IA pour la candidature {}: {}", application.getId(), e.getMessage());
        }
    }

    private String buildScoringPrompt(JobApplication application, JobOffer jobOffer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un expert en recrutement. Analyse la correspondance entre cette offre d'emploi et cette candidature.\n\n");

        sb.append("## OFFRE D'EMPLOI\n");
        sb.append("Titre : ").append(jobOffer.getTitle()).append("\n");
        if (jobOffer.getDescription() != null) sb.append("Description : ").append(jobOffer.getDescription()).append("\n");
        if (jobOffer.getRequirements() != null) sb.append("Exigences : ").append(jobOffer.getRequirements()).append("\n");
        if (jobOffer.getExperienceLevel() != null) sb.append("Niveau d'expérience : ").append(jobOffer.getExperienceLevel()).append("\n");
        if (jobOffer.getContractType() != null) sb.append("Type de contrat : ").append(jobOffer.getContractType()).append("\n");
        if (jobOffer.getCategory() != null) sb.append("Catégorie : ").append(jobOffer.getCategory()).append("\n");

        sb.append("\n## CANDIDATURE\n");
        sb.append("Candidat : ").append(application.getCandidateFirstName()).append(" ").append(application.getCandidateLastName()).append("\n");
        if (application.getCoverLetterText() != null && !application.getCoverLetterText().isBlank()) {
            sb.append("Lettre de motivation :\n").append(application.getCoverLetterText()).append("\n");
        } else {
            sb.append("Lettre de motivation : non fournie\n");
        }
        for (ApplicationFile f : application.getFiles()) {
            String extracted = fileTextExtractor.extractText(f);
            if (extracted != null && !extracted.isBlank()) {
                sb.append("\n### Contenu du fichier — ").append(f.getFileType())
                  .append(" (").append(f.getOriginalFileName()).append(") :\n");
                // Limit to 3000 chars per file to avoid exceeding token limits
                String truncated = extracted.length() > 3000 ? extracted.substring(0, 3000) + "\n[...tronqué]" : extracted;
                sb.append(truncated).append("\n");
            } else {
                sb.append("Fichier joint : ").append(f.getFileType())
                  .append(" (").append(f.getOriginalFileName()).append(") — contenu non extractible\n");
            }
        }

        sb.append("\n## INSTRUCTIONS\n");
        sb.append("Réponds UNIQUEMENT avec un objet JSON valide (sans markdown, sans explication), selon ce format exact :\n");
        sb.append("{\"score\": <nombre entier de 0 à 100>, \"summary\": \"<résumé en 2-3 phrases max>\"}\n");
        sb.append("Le score doit refléter la correspondance globale entre le profil du candidat et les exigences du poste.");

        return sb.toString();
    }

    private String callClaudeApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 512,
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(ANTHROPIC_API_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Réponse Claude invalide: " + response.getStatusCode());
        }

        JsonNode root = parseJson(response.getBody());
        return root.path("content").get(0).path("text").asText();
    }

    private void parseScoringResponse(String responseText, JobApplication application) {
        try {
            // Extraire le JSON de la réponse (au cas où Claude ajoute du texte autour)
            String json = extractJson(responseText);
            JsonNode node = parseJson(json);

            double score = node.path("score").asDouble(-1);
            String summary = node.path("summary").asText(null);

            if (score < 0 || score > 100) {
                log.warn("Score hors limite reçu ({}), ignoré pour candidature {}", score, application.getId());
                return;
            }

            application.setAiScore(score);
            application.setAiScoreSummary(summary);
            jobApplicationRepository.save(application);

            log.info("Score IA calculé pour candidature {}: {}/100", application.getId(), score);
        } catch (Exception e) {
            log.warn("Impossible de parser la réponse IA '{}': {}", responseText, e.getMessage());
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing JSON: " + e.getMessage());
        }
    }
}
