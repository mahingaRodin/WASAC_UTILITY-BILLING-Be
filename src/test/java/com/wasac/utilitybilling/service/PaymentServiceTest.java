package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private com.wasac.utilitybilling.repository.BillRepository billRepository;
    @Mock
    private com.wasac.utilitybilling.repository.PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void shouldRejectNegativePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setBillReference("BILL-1");
        request.setAmountPaid(BigDecimal.valueOf(-5));
        when(billRepository.findByBillReference("BILL-1")).thenReturn(java.util.Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentService.process(request));
    }
}
