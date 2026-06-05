package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.ChargeType;
import com.wasac.utilitybilling.domain.enums.ChargeValueType;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ChargeConfigurationRequest {
    @NotNull
    private ChargeType chargeType;
    private UtilityType utilityType;
    @NotNull
    private ChargeValueType valueType;
    @NotNull
    private BigDecimal value;
    @NotNull
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @NotNull
    private Integer version;
}
