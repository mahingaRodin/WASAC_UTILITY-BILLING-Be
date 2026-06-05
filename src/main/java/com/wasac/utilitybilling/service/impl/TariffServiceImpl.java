package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.domain.TariffTier;
import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.ChargeResponse;
import com.wasac.utilitybilling.dto.TariffResponse;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.TariffMapper;
import com.wasac.utilitybilling.repository.ChargeConfigurationRepository;
import com.wasac.utilitybilling.repository.TariffConfigurationRepository;
import com.wasac.utilitybilling.repository.TariffTierRepository;
import com.wasac.utilitybilling.service.NotificationService;
import com.wasac.utilitybilling.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {
    private final TariffConfigurationRepository tariffConfigurationRepository;
    private final TariffTierRepository tariffTierRepository;
    private final ChargeConfigurationRepository chargeConfigurationRepository;
    private final TariffMapper tariffMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public TariffResponse createTariff(TariffConfigurationRequest request) {
        if (request.getTariffType() == TariffType.FLAT && request.getFlatRate() == null) {
            throw new BadRequestException("Flat rate is required for FLAT tariff.");
        }
        if (request.getTariffType() == TariffType.TIER_BASED &&
                (request.getTiers() == null || request.getTiers().isEmpty())) {
            throw new BadRequestException("Tiers are required for TIER_BASED tariff.");
        }
        validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());
        if (request.getVersion() <= 0) {
            throw new BadRequestException("Version must be positive.");
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
        List<TariffTier> tiers = tariffTierRepository.findByTariffConfiguration_Id(configuration.getId());
        TariffResponse response = tariffMapper.toTariffResponse(configuration, tiers);
        notificationService.broadcastNewTariff(configuration, currentEmail());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TariffResponse> listTariffs() {
        return tariffConfigurationRepository.findAll().stream()
                .map(tariff -> tariffMapper.toTariffResponse(tariff, tariffTierRepository.findByTariffConfiguration_Id(tariff.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TariffResponse getTariffById(UUID id) {
        TariffConfiguration tariff = tariffConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found."));
        return tariffMapper.toTariffResponse(tariff, tariffTierRepository.findByTariffConfiguration_Id(tariff.getId()));
    }

    @Override
    @Transactional
    public TariffResponse deactivateTariff(UUID id) {
        TariffConfiguration tariff = tariffConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found."));
        tariff.setActive(false);
        TariffConfiguration saved = tariffConfigurationRepository.save(tariff);
        return tariffMapper.toTariffResponse(saved, tariffTierRepository.findByTariffConfiguration_Id(saved.getId()));
    }

    @Override
    @Transactional
    public ChargeResponse createCharge(ChargeConfigurationRequest request) {
        validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());
        if (request.getVersion() <= 0) {
            throw new BadRequestException("Version must be positive.");
        }
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
        ChargeConfiguration savedCharge = chargeConfigurationRepository.save(charge);
        notificationService.broadcastChargeChange(savedCharge, currentEmail());
        return tariffMapper.toChargeResponse(savedCharge);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChargeResponse> listCharges() {
        return chargeConfigurationRepository.findAll().stream()
                .map(tariffMapper::toChargeResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChargeResponse deactivateCharge(UUID id) {
        ChargeConfiguration charge = chargeConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge not found."));
        charge.setActive(false);
        return tariffMapper.toChargeResponse(chargeConfigurationRepository.save(charge));
    }

    private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom.isBefore(LocalDate.now())) {
            throw new BadRequestException("effectiveFrom must be today or in the future.");
        }
        if (effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new BadRequestException("effectiveTo must be after effectiveFrom.");
        }
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
