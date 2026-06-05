package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.domain.TariffTier;
import com.wasac.utilitybilling.dto.ChargeResponse;
import com.wasac.utilitybilling.dto.TariffResponse;
import com.wasac.utilitybilling.dto.TariffTierResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TariffMapper {
    public TariffResponse toTariffResponse(TariffConfiguration tariff, List<TariffTier> tiers) {
        return TariffResponse.builder()
                .id(tariff.getId())
                .utilityType(tariff.getUtilityType())
                .tariffType(tariff.getTariffType())
                .flatRate(tariff.getFlatRate())
                .effectiveFrom(tariff.getEffectiveFrom())
                .effectiveTo(tariff.getEffectiveTo())
                .version(tariff.getVersion())
                .active(tariff.isActive())
                .tiers(tiers.stream().map(this::toTierResponse).toList())
                .createdAt(tariff.getCreatedAt())
                .updatedAt(tariff.getUpdatedAt())
                .build();
    }

    public ChargeResponse toChargeResponse(ChargeConfiguration charge) {
        return ChargeResponse.builder()
                .id(charge.getId())
                .chargeType(charge.getChargeType())
                .utilityType(charge.getUtilityType())
                .valueType(charge.getValueType())
                .value(charge.getValue())
                .effectiveFrom(charge.getEffectiveFrom())
                .effectiveTo(charge.getEffectiveTo())
                .version(charge.getVersion())
                .active(charge.isActive())
                .createdAt(charge.getCreatedAt())
                .updatedAt(charge.getUpdatedAt())
                .build();
    }

    private TariffTierResponse toTierResponse(TariffTier tier) {
        return TariffTierResponse.builder()
                .id(tier.getId())
                .lowerBound(tier.getLowerBound())
                .upperBound(tier.getUpperBound())
                .rate(tier.getRate())
                .build();
    }
}
