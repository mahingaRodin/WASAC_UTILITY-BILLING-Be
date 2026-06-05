package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.MeterStatus;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MeterResponse {
    private UUID id;
    private String meterNumber;
    private UtilityType type;
    private LocalDate installationDate;
    private MeterStatus status;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
