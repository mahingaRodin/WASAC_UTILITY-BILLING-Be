package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TariffResponse {
    private UUID id;
    private UtilityType utilityType;
    private TariffType tariffType;
    private BigDecimal flatRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer version;
    private boolean active;
    private List<TariffTierResponse> tiers;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
