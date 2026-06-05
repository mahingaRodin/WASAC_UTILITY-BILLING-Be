package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.ChargeResponse;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.TariffResponse;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;

import java.util.List;
import java.util.UUID;

public interface TariffService {
    TariffResponse createTariff(TariffConfigurationRequest request);
    List<TariffResponse> listTariffs();
    TariffResponse getTariffById(UUID id);
    TariffResponse deactivateTariff(UUID id);
    ChargeResponse createCharge(ChargeConfigurationRequest request);
    List<ChargeResponse> listCharges();
    ChargeResponse deactivateCharge(UUID id);
}
