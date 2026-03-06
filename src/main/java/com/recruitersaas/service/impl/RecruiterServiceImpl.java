package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.RegisterRecruiterRequest;
import com.recruitersaas.dto.request.UpdateRecruiterRequest;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.dto.response.RecruiterResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.mapper.RecruiterMapper;
import com.recruitersaas.model.Plan;
import com.recruitersaas.model.RecruiterProfile;
import com.recruitersaas.model.Subscription;
import com.recruitersaas.model.User;
import com.recruitersaas.model.enums.Role;
import com.recruitersaas.model.enums.SubscriptionStatus;
import com.recruitersaas.repository.PlanRepository;
import com.recruitersaas.repository.RecruiterProfileRepository;
import com.recruitersaas.repository.SubscriptionRepository;
import com.recruitersaas.repository.UserRepository;
import com.recruitersaas.service.RecruiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruiterServiceImpl implements RecruiterService {

    private final UserRepository userRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final RecruiterMapper recruiterMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RecruiterResponse createRecruiter(RegisterRecruiterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà: " + request.getEmail());
        }

        // Créer le compte utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.RECRUITER)
                .enabled(true)
                .build();
        userRepository.save(user);

        // Créer le profil recruteur
        String slug = generateUniqueSlug(request.getCompanyName());
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .companyWebsite(request.getCompanyWebsite())
                .phone(request.getPhone())
                .address(request.getAddress())
                .siret(request.getSiret())
                .slug(slug)
                .build();
        recruiterProfileRepository.save(profile);

        // Créer l'abonnement
        String planId = request.getPlanId();
        Plan plan = planId != null
                ? planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan introuvable: " + planId))
                : planRepository.findAllByActiveTrue().stream().findFirst()
                        .orElseThrow(() -> new BusinessException("Aucun plan actif disponible"));

        Subscription subscription = Subscription.builder()
                .recruiterProfile(profile)
                .plan(plan)
                .status(SubscriptionStatus.TRIAL)
                .startDate(LocalDate.now())
                .trialEndDate(LocalDate.now().plusDays(30))
                .nextBillingDate(LocalDate.now().plusDays(30))
                .build();
        subscriptionRepository.save(subscription);

        return recruiterMapper.toResponse(recruiterProfileRepository.findById(profile.getId()).get());
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterResponse getRecruiterById(String recruiterProfileId) {
        return recruiterMapper.toResponse(findProfileById(recruiterProfileId));
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterResponse getRecruiterBySlug(String slug) {
        RecruiterProfile profile = recruiterProfileRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Recruteur introuvable avec le slug: " + slug));
        return recruiterMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterResponse getMyProfile(String email) {
        RecruiterProfile profile = recruiterProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profil recruteur introuvable"));
        return recruiterMapper.toResponse(profile);
    }

    @Override
    public RecruiterResponse updateRecruiter(String recruiterProfileId, UpdateRecruiterRequest request) {
        RecruiterProfile profile = findProfileById(recruiterProfileId);
        User user = profile.getUser();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Cet email est déjà utilisé: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        userRepository.save(user);

        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getCompanyWebsite() != null) profile.setCompanyWebsite(request.getCompanyWebsite());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());
        if (request.getSiret() != null) profile.setSiret(request.getSiret());
        recruiterProfileRepository.save(profile);

        return recruiterMapper.toResponse(profile);
    }

    @Override
    public void toggleRecruiterStatus(String recruiterProfileId) {
        RecruiterProfile profile = findProfileById(recruiterProfileId);
        User user = profile.getUser();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public void deleteRecruiter(String recruiterProfileId) {
        RecruiterProfile profile = findProfileById(recruiterProfileId);
        recruiterProfileRepository.delete(profile);
        userRepository.delete(profile.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RecruiterResponse> getAllRecruiters(int page, int size) {
        Page<RecruiterProfile> profiles = recruiterProfileRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending()));
        return PageResponse.from(profiles.map(recruiterMapper::toResponse));
    }

    private RecruiterProfile findProfileById(String id) {
        return recruiterProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recruteur introuvable: " + id));
    }

    private String generateUniqueSlug(String companyName) {
        String base = companyName.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        String slug = base;
        int counter = 1;
        while (recruiterProfileRepository.existsBySlug(slug)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
