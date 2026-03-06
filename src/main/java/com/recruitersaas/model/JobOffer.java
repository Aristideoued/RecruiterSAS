package com.recruitersaas.model;

import com.recruitersaas.model.enums.JobOfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_offers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_profile_id", nullable = false)
    private RecruiterProfile recruiterProfile;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private String location;
    private String contractType;    // CDI, CDD, Stage, Alternance, Freelance
    private String workMode;        // Remote, Hybride, Présentiel
    private String salaryRange;
    private String experienceLevel; // Junior, Mid, Senior
    private String category;        // IT, Marketing, Finance...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobOfferStatus status = JobOfferStatus.DRAFT;

    private LocalDate publishedAt;
    private LocalDate closingDate;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<JobApplication> applications = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
