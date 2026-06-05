package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private UserStatus status;
    private UserRole role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
