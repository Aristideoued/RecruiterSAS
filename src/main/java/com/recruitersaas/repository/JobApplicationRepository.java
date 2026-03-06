package com.recruitersaas.repository;

import com.recruitersaas.model.JobApplication;
import com.recruitersaas.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, String> {

    Page<JobApplication> findAllByJobOfferId(String jobOfferId, Pageable pageable);

    Page<JobApplication> findAllByJobOfferIdAndStatus(String jobOfferId, ApplicationStatus status, Pageable pageable);

    boolean existsByJobOfferIdAndCandidateEmail(String jobOfferId, String email);

    long countByJobOfferId(String jobOfferId);

    long countByJobOfferIdAndStatus(String jobOfferId, ApplicationStatus status);

    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId")
    Page<JobApplication> findAllByRecruiterProfileId(String recruiterProfileId, Pageable pageable);

    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId AND a.status = :status")
    Page<JobApplication> findAllByRecruiterProfileIdAndStatus(String recruiterProfileId, ApplicationStatus status, Pageable pageable);

    @Query("SELECT COUNT(a) FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId")
    long countByRecruiterProfileId(String recruiterProfileId);

    @Query("SELECT COUNT(a) FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId AND a.status = 'PENDING'")
    long countPendingByRecruiterProfileId(String recruiterProfileId);

    // Requêtes triées par score IA (nulls en dernier)
    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.id = :jobOfferId ORDER BY a.aiScore DESC NULLS LAST, a.submittedAt DESC")
    Page<JobApplication> findAllByJobOfferIdSortedByScore(String jobOfferId, Pageable pageable);

    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.id = :jobOfferId AND a.status = :status ORDER BY a.aiScore DESC NULLS LAST, a.submittedAt DESC")
    Page<JobApplication> findAllByJobOfferIdAndStatusSortedByScore(String jobOfferId, ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId ORDER BY a.aiScore DESC NULLS LAST, a.submittedAt DESC")
    Page<JobApplication> findAllByRecruiterProfileIdSortedByScore(String recruiterProfileId, Pageable pageable);

    @Query("SELECT a FROM JobApplication a WHERE a.jobOffer.recruiterProfile.id = :recruiterProfileId AND a.status = :status ORDER BY a.aiScore DESC NULLS LAST, a.submittedAt DESC")
    Page<JobApplication> findAllByRecruiterProfileIdAndStatusSortedByScore(String recruiterProfileId, ApplicationStatus status, Pageable pageable);
}
