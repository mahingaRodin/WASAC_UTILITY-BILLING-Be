package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.service.impl.TariffServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TariffServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.TariffConfigurationRepository tariffConfigurationRepository;
    @Mock
    private com.wasac.utilitybilling.repository.TariffTierRepository tariffTierRepository;
    @Mock
    private com.wasac.utilitybilling.repository.ChargeConfigurationRepository chargeConfigurationRepository;
    @InjectMocks
    private TariffServiceImpl tariffService;

    @Test
    void shouldRejectFlatWithoutRate() {
        TariffConfigurationRequest request = new TariffConfigurationRequest();
        request.setTariffType(TariffType.FLAT);
        assertThrows(RuntimeException.class, () -> tariffService.createTariff(request));
    }
}
