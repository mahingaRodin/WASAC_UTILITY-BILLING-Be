package com.wasac.utilitybilling.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class MeterReadingResponse {
    private UUID id;
    private UUID meterId;
    private String meterNumber;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private LocalDate readingDate;
    private String recordedByName;
    private String recordedByEmail;
    private LocalDateTime createdAt;
}
