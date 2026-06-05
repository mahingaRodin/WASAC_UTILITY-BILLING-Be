package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.ChargeType;
import com.wasac.utilitybilling.domain.enums.ChargeValueType;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ChargeResponse {
    private UUID id;
    private ChargeType chargeType;
    private UtilityType utilityType;
    private ChargeValueType valueType;
    private BigDecimal value;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer version;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
