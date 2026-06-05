package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {
    @NotNull
    private UserStatus status;
}
