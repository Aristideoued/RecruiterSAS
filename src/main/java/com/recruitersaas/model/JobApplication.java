package com.recruitersaas.model;

import com.recruitersaas.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    @Column(nullable = false)
    private String candidateFirstName;

    @Column(nullable = false)
    private String candidateLastName;

    @Column(nullable = false)
    private String candidateEmail;

    private String candidatePhone;
    private String candidateLinkedin;

    @Column(columnDefinition = "TEXT")
    private String coverLetterText;

    @Column(columnDefinition = "TEXT")
    private String recruiterNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private Integer rating; // 1 à 5

    @OneToMany(mappedBy = "jobApplication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApplicationFile> files = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
