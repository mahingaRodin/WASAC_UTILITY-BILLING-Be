package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.dto.UpdateMeterStatusRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.service.impl.MeterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.MeterRepository meterRepository;
    @Mock
    private com.wasac.utilitybilling.repository.CustomerRepository customerRepository;
    @Mock
    private com.wasac.utilitybilling.repository.MeterReadingRepository meterReadingRepository;
    @Mock
    private com.wasac.utilitybilling.repository.BillRepository billRepository;
    @Mock
    private CurrentCustomerResolver currentCustomerResolver;
    @Mock
    private com.wasac.utilitybilling.mapper.MeterMapper meterMapper;
    @InjectMocks
    private MeterServiceImpl meterService;

    @Test
    void createShouldRejectDuplicateMeterNumber() {
        MeterRequest request = new MeterRequest();
        request.setMeterNumber("MTR-001");
        when(meterRepository.existsByMeterNumber("MTR-001")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> meterService.create(request));
    }

    @Test
    void createShouldRejectFutureInstallationDate() {
        MeterRequest request = new MeterRequest();
        request.setMeterNumber("MTR-010");
        request.setInstallationDate(LocalDate.now().plusDays(2));
        request.setCustomerId(java.util.UUID.randomUUID());
        when(meterRepository.existsByMeterNumber("MTR-010")).thenReturn(false);
        assertThrows(BadRequestException.class, () -> meterService.create(request));
    }

    @Test
    void updateStatusShouldFailWhenMeterMissing() {
        java.util.UUID id = java.util.UUID.randomUUID();
        UpdateMeterStatusRequest request = new UpdateMeterStatusRequest();
        request.setStatus(com.wasac.utilitybilling.domain.enums.MeterStatus.ACTIVE);
        when(meterRepository.findById(id)).thenReturn(java.util.Optional.empty());
        assertThrows(RuntimeException.class, () -> meterService.updateStatus(id, request));
    }
}
