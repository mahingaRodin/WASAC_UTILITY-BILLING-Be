package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserView {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private UserStatus status;
    private UserRole role;
    private boolean emailVerified;
}
