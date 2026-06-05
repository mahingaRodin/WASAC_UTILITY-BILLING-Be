package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.CustomerStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {
    @NotBlank
    private String nationalId;
    @NotBlank
    private String fullName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String phone;
    @NotNull
    private CustomerStatus status;
    @NotNull
    @Valid
    private AddressDTO address;
}
