package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.MeterStatus;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class MeterRequest {
    @NotBlank
    private String meterNumber;
    @NotNull
    private UtilityType type;
    @NotNull
    private LocalDate installationDate;
    @NotNull
    private MeterStatus status;
    @NotNull
    private UUID customerId;
}
