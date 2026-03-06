package com.recruitersaas.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Builder.Default
    private int maxJobOffers = 5;       // -1 = illimité

    @Builder.Default
    private int maxApplicationsPerOffer = 50;  // -1 = illimité

    @Builder.Default
    private boolean cvParsingEnabled = false;

    @Builder.Default
    private boolean analyticsEnabled = false;

    @Builder.Default
    private boolean customBrandingEnabled = false;

    @Builder.Default
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
