package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.enums.TariffType;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
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
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private TariffServiceImpl tariffService;
    @Mock
    private com.wasac.utilitybilling.mapper.TariffMapper tariffMapper;

    @Test
    void shouldRejectFlatWithoutRate() {
        TariffConfigurationRequest request = new TariffConfigurationRequest();
        request.setTariffType(TariffType.FLAT);
        assertThrows(BadRequestException.class, () -> tariffService.createTariff(request));
    }

    @Test
    void shouldRejectEffectiveFromInPast() {
        TariffConfigurationRequest request = new TariffConfigurationRequest();
        request.setTariffType(TariffType.FLAT);
        request.setFlatRate(java.math.BigDecimal.ONE);
        request.setUtilityType(com.wasac.utilitybilling.domain.enums.UtilityType.WATER);
        request.setVersion(1);
        request.setEffectiveFrom(java.time.LocalDate.now().minusDays(1));
        assertThrows(BadRequestException.class, () -> tariffService.createTariff(request));
    }
}
