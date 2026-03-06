package com.recruitersaas.dto.response;

import com.recruitersaas.model.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private long expiresIn;
}
