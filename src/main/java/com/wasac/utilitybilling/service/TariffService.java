package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;

public interface TariffService {
    TariffConfiguration createTariff(TariffConfigurationRequest request);
    ChargeConfiguration createCharge(ChargeConfigurationRequest request);
}
