package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.service.impl.MeterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.MeterRepository meterRepository;
    @Mock
    private com.wasac.utilitybilling.repository.CustomerRepository customerRepository;
    @InjectMocks
    private MeterServiceImpl meterService;

    @Test
    void createShouldRejectDuplicateMeterNumber() {
        MeterRequest request = new MeterRequest();
        request.setMeterNumber("MTR-001");
        when(meterRepository.existsByMeterNumber("MTR-001")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> meterService.create(request));
    }
}
