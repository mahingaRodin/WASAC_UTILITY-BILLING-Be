package com.wasac.utilitybilling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class MeterReadingRequest {
    @NotNull
    private UUID meterId;
    @NotNull
    private BigDecimal previousReading;
    @NotNull
    private BigDecimal currentReading;
    @NotNull
    private LocalDate readingDate;
}
