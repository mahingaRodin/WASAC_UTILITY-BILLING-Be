package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.CustomerStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CustomerResponse {
    private UUID id;
    private String nationalId;
    private String fullName;
    private String email;
    private String phone;
    private CustomerStatus status;
    private AddressDTO address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
