package com.recruitersaas.service;

import com.recruitersaas.dto.request.LoginRequest;
import com.recruitersaas.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}
