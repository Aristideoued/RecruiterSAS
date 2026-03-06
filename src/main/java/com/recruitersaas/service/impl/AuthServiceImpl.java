package com.recruitersaas.service.impl;

import com.recruitersaas.dto.request.LoginRequest;
import com.recruitersaas.dto.response.AuthResponse;
import com.recruitersaas.exception.BusinessException;
import com.recruitersaas.exception.ResourceNotFoundException;
import com.recruitersaas.model.User;
import com.recruitersaas.repository.UserRepository;
import com.recruitersaas.security.JwtService;
import com.recruitersaas.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new BusinessException("Email ou mot de passe incorrect");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!user.isEnabled()) {
            throw new BusinessException("Votre compte a été désactivé. Contactez l'administrateur.");
        }

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpiration())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        String newToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(jwtService.getExpiration())
                .build();
    }
}
