package com.recruitersaas.repository;

import com.recruitersaas.model.JobOffer;
import com.recruitersaas.model.enums.JobOfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, String> {

    Page<JobOffer> findAllByRecruiterProfileId(String recruiterProfileId, Pageable pageable);

    Page<JobOffer> findAllByRecruiterProfileIdAndStatus(String recruiterProfileId, JobOfferStatus status, Pageable pageable);

    long countByRecruiterProfileId(String recruiterProfileId);

    long countByRecruiterProfileIdAndStatus(String recruiterProfileId, JobOfferStatus status);

    @Query("SELECT jo FROM JobOffer jo WHERE jo.recruiterProfile.slug = :slug AND jo.status = 'PUBLISHED'")
    Page<JobOffer> findPublishedByRecruiterSlug(String slug, Pageable pageable);

    boolean existsByIdAndRecruiterProfileId(String id, String recruiterProfileId);
}
