package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.domain.enums.MeterStatus;
import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.service.impl.MeterReadingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.MeterRepository meterRepository;
    @Mock
    private com.wasac.utilitybilling.repository.MeterReadingRepository meterReadingRepository;
    @Mock
    private com.wasac.utilitybilling.mapper.MeterReadingMapper meterReadingMapper;
    @Mock
    private com.wasac.utilitybilling.repository.UserRepository userRepository;
    @InjectMocks
    private MeterReadingServiceImpl meterReadingService;

    @Test
    void shouldRejectInactiveMeter() {
        UUID meterId = UUID.randomUUID();
        Meter meter = Meter.builder().id(meterId).status(MeterStatus.INACTIVE).build();
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(meterId);
        request.setPreviousReading(BigDecimal.ONE);
        request.setCurrentReading(BigDecimal.TEN);
        request.setReadingDate(java.time.LocalDate.now());
        when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
        assertThrows(BadRequestException.class, () -> meterReadingService.create(request));
    }

    @Test
    void shouldRejectPreviousMismatchWithLastReading() {
        UUID meterId = UUID.randomUUID();
        Meter meter = Meter.builder().id(meterId).status(MeterStatus.ACTIVE).build();
        MeterReadingRequest request = new MeterReadingRequest();
        request.setMeterId(meterId);
        request.setPreviousReading(BigDecimal.valueOf(15));
        request.setCurrentReading(BigDecimal.valueOf(20));
        request.setReadingDate(java.time.LocalDate.now());

        com.wasac.utilitybilling.domain.MeterReading last = com.wasac.utilitybilling.domain.MeterReading.builder()
                .meter(meter)
                .currentReading(BigDecimal.valueOf(10))
                .build();

        when(meterRepository.findById(meterId)).thenReturn(Optional.of(meter));
        when(meterReadingRepository.findTopByMeter_IdOrderByReadingDateDesc(meterId)).thenReturn(Optional.of(last));
        assertThrows(BadRequestException.class, () -> meterReadingService.create(request));
    }
}
