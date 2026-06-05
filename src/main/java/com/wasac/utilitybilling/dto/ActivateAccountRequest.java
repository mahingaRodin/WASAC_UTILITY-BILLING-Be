package com.wasac.utilitybilling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateAccountRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String temporaryPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
