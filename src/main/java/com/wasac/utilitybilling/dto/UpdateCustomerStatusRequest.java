package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerStatusRequest {
    @NotNull
    private CustomerStatus status;
}
