package com.wasac.utilitybilling.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class TariffTierResponse {
    private UUID id;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private BigDecimal rate;
}
