package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    @NotNull
    private UserRole role;
}
