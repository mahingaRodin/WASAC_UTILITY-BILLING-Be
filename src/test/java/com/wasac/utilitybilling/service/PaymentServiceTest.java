package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.BillApprovalStatus;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.domain.enums.PaymentMethod;
import com.wasac.utilitybilling.domain.enums.PaymentStatus;
import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.PaymentRepository;
import com.wasac.utilitybilling.mapper.PaymentMapper;
import com.wasac.utilitybilling.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {
    @Mock
    private BillRepository billRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CurrentCustomerResolver currentCustomerResolver;
    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Bill bill(String ref, BillStatus status, BillApprovalStatus approval, BigDecimal due, BigDecimal paid) {
        return Bill.builder()
                .id(UUID.randomUUID())
                .billReference(ref)
                .status(status)
                .approvalStatus(approval)
                .amountDue(due)
                .paidAmount(paid)
                .outstandingBalance(due.subtract(paid))
                .billingMonth(1)
                .billingYear(2026)
                .customer(Customer.builder().fullName("Test Customer").email("c@wasac.rw").build())
                .build();
    }

    private PaymentRequest request(String ref, BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setBillReference(ref);
        request.setAmountPaid(amount);
        request.setPaymentDate(LocalDate.now());
        request.setPaymentMethod(PaymentMethod.CASH);
        return request;
    }

    @Test
    void shouldRejectPaymentForUnapprovedBill() {
        Bill bill = bill("BILL-0", BillStatus.UNPAID, BillApprovalStatus.PENDING, BigDecimal.TEN, BigDecimal.ZERO);
        when(billRepository.findByBillReference("BILL-0")).thenReturn(Optional.of(bill));
        assertThrows(BadRequestException.class, () -> paymentService.process(request("BILL-0", BigDecimal.ONE)));
    }

    @Test
    void shouldRejectNegativePayment() {
        Bill bill = bill("BILL-1", BillStatus.UNPAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.ZERO);
        when(billRepository.findByBillReference("BILL-1")).thenReturn(Optional.of(bill));
        assertThrows(BadRequestException.class, () -> paymentService.process(request("BILL-1", BigDecimal.valueOf(-5))));
    }

    @Test
    void shouldRejectAlreadyPaidBill() {
        Bill bill = bill("BILL-2", BillStatus.PAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.TEN);
        when(billRepository.findByBillReference("BILL-2")).thenReturn(Optional.of(bill));
        assertThrows(BadRequestException.class, () -> paymentService.process(request("BILL-2", BigDecimal.ONE)));
    }

    @Test
    void shouldRejectPaymentExceedingOutstanding() {
        Bill bill = bill("BILL-3", BillStatus.PARTIALLY_PAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.valueOf(5));
        when(billRepository.findByBillReference("BILL-3")).thenReturn(Optional.of(bill));
        when(paymentRepository.findByBill_IdAndStatus(any(), any())).thenReturn(List.of());
        assertThrows(BadRequestException.class, () -> paymentService.process(request("BILL-3", BigDecimal.valueOf(6))));
    }

    @Test
    void shouldSubmitPaymentAsPendingAndNotifyFinance() {
        Bill bill = bill("BILL-4", BillStatus.UNPAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.ZERO);
        when(billRepository.findByBillReference("BILL-4")).thenReturn(Optional.of(bill));
        when(paymentRepository.findByBill_IdAndStatus(any(), any())).thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.process(request("BILL-4", BigDecimal.valueOf(4)));

        verify(notificationService, times(1)).notifyFinanceOfPendingPayment(any(Payment.class));
        verify(notificationService, times(1)).sendPendingPaymentNotification(any(Bill.class), any(Payment.class));
        verify(notificationService, org.mockito.Mockito.never()).sendFullPaymentNotification(any(), any(), any());
    }

    @Test
    void approvingFullPaymentMarksBillPaidAndNotifiesCustomer() {
        Bill bill = bill("BILL-5", BillStatus.UNPAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.ZERO);
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bill(bill)
                .billReference("BILL-5")
                .amountPaid(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.approve(payment.getId());

        verify(notificationService, times(1)).sendFullPaymentNotification(any(Bill.class), any(Customer.class), any(Payment.class));
    }

    @Test
    void approvingPartialPaymentNotifiesCustomerOfRemainingBalance() {
        Bill bill = bill("BILL-6", BillStatus.UNPAID, BillApprovalStatus.APPROVED, BigDecimal.TEN, BigDecimal.ZERO);
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .bill(bill)
                .billReference("BILL-6")
                .amountPaid(BigDecimal.valueOf(4))
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.approve(payment.getId());

        verify(notificationService, times(1)).sendPartialPaymentNotification(any(Bill.class), any(Payment.class));
        verify(notificationService, org.mockito.Mockito.never()).sendFullPaymentNotification(any(), any(), any());
    }
}
