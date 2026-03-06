package com.recruitersaas.init;

import com.recruitersaas.model.Plan;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.Subscription;
import com.recruitersaas.model.User;
import com.recruitersaas.model.enums.Role;
import com.recruitersaas.model.enums.SubscriptionStatus;
import com.recruitersaas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initPlans();
        initSuperAdmin();
        initTestRecruiter();
        printStartupInfo();
    }

    private void initPlans() {
        if (planRepository.count() > 0) return;

        List<Plan> plans = List.of(
                Plan.builder()
                        .name("Starter")
                        .description("Idéal pour les petites entreprises. Jusqu'à 3 offres publiées simultanément.")
                        .monthlyPrice(new BigDecimal("29.00"))
                        .maxJobOffers(3)
                        .maxApplicationsPerOffer(30)
                        .cvParsingEnabled(false)
                        .analyticsEnabled(false)
                        .customBrandingEnabled(false)
                        .active(true)
                        .build(),
                Plan.builder()
                        .name("Pro")
                        .description("Pour les entreprises en croissance. Jusqu'à 10 offres publiées.")
                        .monthlyPrice(new BigDecimal("79.00"))
                        .maxJobOffers(10)
                        .maxApplicationsPerOffer(100)
                        .cvParsingEnabled(true)
                        .analyticsEnabled(true)
                        .customBrandingEnabled(false)
                        .active(true)
                        .build(),
                Plan.builder()
                        .name("Enterprise")
                        .description("Offres illimitées, toutes fonctionnalités incluses.")
                        .monthlyPrice(new BigDecimal("199.00"))
                        .maxJobOffers(-1)
                        .maxApplicationsPerOffer(-1)
                        .cvParsingEnabled(true)
                        .analyticsEnabled(true)
                        .customBrandingEnabled(true)
                        .active(true)
                        .build()
        );

        planRepository.saveAll(plans);
        log.info("Plans créés: Starter (29€), Pro (79€), Enterprise (199€)");
    }

    private void initSuperAdmin() {
        String adminEmail = "admin@recruitersaas.com";
        if (userRepository.existsByEmail(adminEmail)) return;

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@1234"))
                .firstName("Super")
                .lastName("Admin")
                .role(Role.SUPER_ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.info("Super Admin créé: {}", adminEmail);
    }

    private void initTestRecruiter() {
        String recruiterEmail = "recruteur@demo.com";
        if (userRepository.existsByEmail(recruiterEmail)) return;

        Plan proPlan = planRepository.findByName("Pro")
                .orElse(planRepository.findAllByActiveTrue().get(0));

        User recruiterUser = User.builder()
                .email(recruiterEmail)
                .password(passwordEncoder.encode("Recruiter@1234"))
                .firstName("Jean")
                .lastName("Martin")
                .role(Role.RECRUITER)
                .enabled(true)
                .build();
        userRepository.save(recruiterUser);

        RecruiterProfile profile = RecruiterProfile.builder()
                .user(recruiterUser)
                .companyName("TechCorp Solutions")
                .companyWebsite("https://techcorp.demo")
                .phone("+33 1 23 45 67 89")
                .address("12 rue de la Paix, 75001 Paris")
                .slug("techcorp-solutions")
                .build();
        recruiterProfileRepository.save(profile);

        Subscription subscription = Subscription.builder()
                .recruiterProfile(profile)
                .plan(proPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .build();
        subscriptionRepository.save(subscription);

        log.info("Recruteur demo créé: {}", recruiterEmail);
    }

    private void printStartupInfo() {
        log.info("\n");
        log.info("============================================================");
        log.info("   RECRUITER SaaS — Backend démarré avec succès !");
        log.info("============================================================");
        log.info("  API BASE URL : http://localhost:8080");
        log.info("  H2 Console   : http://localhost:8080/h2-console");
        log.info("    JDBC URL   : jdbc:h2:mem:recruitersaasdb");
        log.info("    User: sa | Password: (vide)");
        log.info("------------------------------------------------------------");
        log.info("  Comptes disponibles:");
        log.info("  [SUPER ADMIN]  admin@recruitersaas.com  / Admin@1234");
        log.info("  [RECRUTEUR]    recruteur@demo.com       / Recruiter@1234");
        log.info("------------------------------------------------------------");
        log.info("  Endpoints principaux:");
        log.info("  POST /api/auth/login");
        log.info("  GET  /api/public/recruiters/{slug}/offers");
        log.info("  POST /api/public/offers/{id}/apply");
        log.info("  GET  /api/recruiter/dashboard   (Bearer token)");
        log.info("  GET  /api/admin/dashboard       (Bearer token admin)");
        log.info("============================================================");
    }
}
