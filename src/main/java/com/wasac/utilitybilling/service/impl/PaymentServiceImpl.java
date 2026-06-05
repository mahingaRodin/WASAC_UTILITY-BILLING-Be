package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.PaymentRepository;
import com.wasac.utilitybilling.service.NotificationService;
import com.wasac.utilitybilling.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Payment process(PaymentRequest request) {
        Bill bill = billRepository.findByBillReference(request.getBillReference())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));

        if (request.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be positive.");
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .bill(bill)
                .billReference(bill.getBillReference())
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .build());

        bill.setPaidAmount(bill.getPaidAmount().add(request.getAmountPaid()));
        BigDecimal outstanding = bill.getAmountDue().subtract(bill.getPaidAmount());
        bill.setOutstandingBalance(outstanding.max(BigDecimal.ZERO));
        bill.setStatus(bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0
                ? BillStatus.PAID
                : BillStatus.PARTIALLY_PAID);

        Bill savedBill = billRepository.save(bill);
        if (savedBill.getStatus() == BillStatus.PAID) {
            notificationService.sendFullPaymentNotification(savedBill, savedBill.getCustomer());
        }
        return payment;
    }
}
