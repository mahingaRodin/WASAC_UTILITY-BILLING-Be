package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.domain.enums.UtilityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TariffConfigurationRequest {
    @NotNull
    private UtilityType utilityType;
    @NotNull
    private TariffType tariffType;
    private BigDecimal flatRate;
    @NotNull
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    @NotNull
    private Integer version;
    @Valid
    private List<TariffTierRequest> tiers;
}
