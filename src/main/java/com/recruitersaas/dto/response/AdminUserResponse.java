package com.recruitersaas.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private LocalDateTime createdAt;
}
