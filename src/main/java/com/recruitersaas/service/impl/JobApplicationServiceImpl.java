package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.ApplicationStatusUpdateRequest;
import com.recruitersaas.dto.request.JobApplicationRequest;
import com.recruitersaas.dto.response.JobApplicationResponse;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.mapper.JobApplicationMapper;
import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.JobOffer;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.enums.ApplicationStatus;
import com.recruitersaas.model.enums.FileType;
import com.recruitersaas.model.enums.JobOfferStatus;
import com.recruitersaas.repository.JobApplicationRepository;
import com.recruitersaas.repository.JobOfferRepository;
import com.recruitersaas.repository.RecruiterProfileRepository;
import com.recruitersaas.service.AiScoringService;
import com.recruitersaas.service.EmailService;
import com.recruitersaas.service.FileStorageService;
import com.recruitersaas.service.JobApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;
    private final AiScoringService aiScoringService;

    @Override
    public JobApplicationResponse submitApplication(String jobOfferId, JobApplicationRequest request, List<MultipartFile> files) {
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre introuvable: " + jobOfferId));

        if (jobOffer.getStatus() != JobOfferStatus.PUBLISHED) {
            throw new BusinessException("Cette offre n'est plus disponible aux candidatures.");
        }

        if (jobApplicationRepository.existsByJobOfferIdAndCandidateEmail(jobOfferId, request.getCandidateEmail())) {
            throw new BusinessException("Vous avez déjà postulé à cette offre avec cet email.");
        }

        JobApplication application = jobApplicationMapper.toEntity(request);
        application.setJobOffer(jobOffer);
        application = jobApplicationRepository.save(application);

        // Stocker les fichiers
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    FileType fileType = detectFileType(file.getOriginalFilename());
                    fileStorageService.storeFile(file, application, fileType);
                }
            }
        }

        // Recharger avec les fichiers
        JobApplication saved = jobApplicationRepository.findById(application.getId()).get();

        // Scoring IA asynchrone (non bloquant)
        aiScoringService.scoreApplicationAsync(saved, jobOffer);

        // Extraire les données dans la transaction (avant @Async qui ferme la session)
        String recruiterEmail = jobOffer.getRecruiterProfile().getUser().getEmail();
        String companyName    = jobOffer.getRecruiterProfile().getCompanyName();
        String candidateName  = saved.getCandidateFirstName() + " " + saved.getCandidateLastName();

        emailService.sendNewApplicationNotification(
                recruiterEmail, candidateName, saved.getCandidateEmail(),
                saved.getCandidatePhone(), jobOffer.getTitle(), companyName);

        return jobApplicationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(String id, String recruiterEmail) {
        JobApplication application = findById(id);
        assertRecruiterOwnsApplication(application, recruiterEmail);
        return jobApplicationMapper.toResponse(application);
    }

    @Override
    public JobApplicationResponse updateApplicationStatus(String id, ApplicationStatusUpdateRequest request, String recruiterEmail) {
        JobApplication application = findById(id);
        assertRecruiterOwnsApplication(application, recruiterEmail);

        application.setStatus(request.getStatus());
        if (request.getRecruiterNotes() != null) application.setRecruiterNotes(request.getRecruiterNotes());
        if (request.getRating() != null) application.setRating(request.getRating());

        if (request.getStatus() != ApplicationStatus.PENDING) {
            application.setReviewedAt(LocalDateTime.now());
        }

        JobApplication updated = jobApplicationRepository.save(application);

        // Extraire les données dans la transaction (avant @Async qui ferme la session)
        String offerTitle  = updated.getJobOffer().getTitle();
        String companyName = updated.getJobOffer().getRecruiterProfile().getCompanyName();

        emailService.sendStatusChangeNotification(
                updated.getCandidateEmail(), updated.getCandidateFirstName(),
                offerTitle, companyName, updated.getStatus());

        return jobApplicationMapper.toResponse(updated);
    }

    @Override
    public void deleteApplication(String id, String recruiterEmail) {
        JobApplication application = findById(id);
        assertRecruiterOwnsApplication(application, recruiterEmail);
        jobApplicationRepository.delete(application);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> getApplicationsByJobOffer(
            String jobOfferId, ApplicationStatus status, int page, int size, String recruiterEmail) {

        // Vérifier que l'offre appartient au recruteur
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre introuvable: " + jobOfferId));
        assertRecruiterOwnsOffer(jobOffer, recruiterEmail);

        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<JobApplication> applications = status != null
                ? jobApplicationRepository.findAllByJobOfferIdAndStatus(jobOfferId, status, pageable)
                : jobApplicationRepository.findAllByJobOfferId(jobOfferId, pageable);

        return PageResponse.from(applications.map(jobApplicationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> getAllRecruiterApplications(String recruiterEmail, ApplicationStatus status, int page, int size) {
        RecruiterProfile profile = getProfileByEmail(recruiterEmail);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());

        Page<JobApplication> applications = status != null
                ? jobApplicationRepository.findAllByRecruiterProfileIdAndStatus(profile.getId(), status, pageable)
                : jobApplicationRepository.findAllByRecruiterProfileId(profile.getId(), pageable);

        return PageResponse.from(applications.map(jobApplicationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> getAllApplications(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        return PageResponse.from(jobApplicationRepository.findAll(pageable).map(jobApplicationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> getApplicationsByJobOfferSortedByScore(
            String jobOfferId, ApplicationStatus status, int page, int size, String recruiterEmail) {

        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Offre introuvable: " + jobOfferId));
        assertRecruiterOwnsOffer(jobOffer, recruiterEmail);

        PageRequest pageable = PageRequest.of(page, size);
        Page<JobApplication> applications = status != null
                ? jobApplicationRepository.findAllByJobOfferIdAndStatusSortedByScore(jobOfferId, status, pageable)
                : jobApplicationRepository.findAllByJobOfferIdSortedByScore(jobOfferId, pageable);

        return PageResponse.from(applications.map(jobApplicationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JobApplicationResponse> getAllRecruiterApplicationsSortedByScore(
            String recruiterEmail, ApplicationStatus status, int page, int size) {

        RecruiterProfile profile = getProfileByEmail(recruiterEmail);
        PageRequest pageable = PageRequest.of(page, size);

        Page<JobApplication> applications = status != null
                ? jobApplicationRepository.findAllByRecruiterProfileIdAndStatusSortedByScore(profile.getId(), status, pageable)
                : jobApplicationRepository.findAllByRecruiterProfileIdSortedByScore(profile.getId(), pageable);

        return PageResponse.from(applications.map(jobApplicationMapper::toResponse));
    }

    @Override
    public void triggerScoring(String applicationId, String recruiterEmail) {
        JobApplication application = findById(applicationId);
        assertRecruiterOwnsApplication(application, recruiterEmail);
        aiScoringService.scoreApplicationAsync(application, application.getJobOffer());
    }

    // --- Helpers ---

    private JobApplication findById(String id) {
        return jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature introuvable: " + id));
    }

    private RecruiterProfile getProfileByEmail(String email) {
        return recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profil recruteur introuvable"));
    }

    private void assertRecruiterOwnsApplication(JobApplication application, String recruiterEmail) {
        assertRecruiterOwnsOffer(application.getJobOffer(), recruiterEmail);
    }

    private void assertRecruiterOwnsOffer(JobOffer offer, String recruiterEmail) {
        RecruiterProfile profile = getProfileByEmail(recruiterEmail);
        if (!offer.getRecruiterProfile().getId().equals(profile.getId())) {
            throw new BusinessException("Accès non autorisé à cette candidature");
        }
    }

    private FileType detectFileType(String filename) {
        if (filename == null) return FileType.OTHER;
        String lower = filename.toLowerCase();
        if (lower.contains("cv") || lower.contains("resume") || lower.contains("curriculum")) return FileType.CV;
        if (lower.contains("lm") || lower.contains("lettre") || lower.contains("cover") || lower.contains("motivation")) return FileType.COVER_LETTER;
        if (lower.contains("portfolio")) return FileType.PORTFOLIO;
        return FileType.OTHER;
    }
}
