package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.CreateAdminRequest;
import com.recruitersaas.dto.request.UpdateAdminRequest;
import com.recruitersaas.dto.response.AdminUserResponse;
import com.recruitersaas.dto.response.PageResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.model.User;
import com.recruitersaas.model.enums.Role;
import com.recruitersaas.repository.UserRepository;
import com.recruitersaas.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllAdmins(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(
                userRepository.findAllByRole(Role.SUPER_ADMIN, pageable)
                              .map(this::toResponse)
        );
    }

    @Override
    public AdminUserResponse createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte avec cet email existe déjà");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.SUPER_ADMIN)
                .enabled(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    @Override
    public AdminUserResponse updateAdmin(String id, UpdateAdminRequest request) {
        User user = findById(id);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Cet email est déjà utilisé");
            }
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    @Override
    public void toggleAdmin(String id) {
        User user = findById(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    public void deleteAdmin(String id) {
        userRepository.delete(findById(id));
    }

    private User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Administrateur introuvable: " + id));
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
