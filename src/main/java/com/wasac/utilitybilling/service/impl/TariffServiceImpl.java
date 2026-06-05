package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.domain.TariffTier;
import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.repository.ChargeConfigurationRepository;
import com.wasac.utilitybilling.repository.TariffConfigurationRepository;
import com.wasac.utilitybilling.repository.TariffTierRepository;
import com.wasac.utilitybilling.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {
    private final TariffConfigurationRepository tariffConfigurationRepository;
    private final TariffTierRepository tariffTierRepository;
    private final ChargeConfigurationRepository chargeConfigurationRepository;

    @Override
    @Transactional
    public TariffConfiguration createTariff(TariffConfigurationRequest request) {
        if (request.getTariffType() == TariffType.FLAT && request.getFlatRate() == null) {
            throw new BadRequestException("Flat rate is required for FLAT tariff.");
        }
        if (request.getTariffType() == TariffType.TIER_BASED &&
                (request.getTiers() == null || request.getTiers().isEmpty())) {
            throw new BadRequestException("Tiers are required for TIER_BASED tariff.");
        }

        TariffConfiguration configuration = tariffConfigurationRepository.save(TariffConfiguration.builder()
                .utilityType(request.getUtilityType())
                .tariffType(request.getTariffType())
                .flatRate(request.getFlatRate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .version(request.getVersion())
                .active(true)
                .build());

        if (request.getTariffType() == TariffType.TIER_BASED) {
            for (var tier : request.getTiers()) {
                tariffTierRepository.save(TariffTier.builder()
                        .tariffConfiguration(configuration)
                        .lowerBound(tier.getLowerBound())
                        .upperBound(tier.getUpperBound())
                        .rate(tier.getRate())
                        .build());
            }
        }
        return configuration;
    }

    @Override
    @Transactional
    public ChargeConfiguration createCharge(ChargeConfigurationRequest request) {
        ChargeConfiguration charge = ChargeConfiguration.builder()
                .chargeType(request.getChargeType())
                .utilityType(request.getUtilityType())
                .valueType(request.getValueType())
                .value(request.getValue())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .version(request.getVersion())
                .active(true)
                .build();
        return chargeConfigurationRepository.save(charge);
    }
}
