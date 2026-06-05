package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResponse toResponse(Payment payment) {
        return toResponse(payment, null, null);
    }

    public PaymentResponse toResponse(Payment payment, String message, String receiptUrl) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .billReference(payment.getBillReference())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .billStatus(payment.getBill().getStatus())
                .amountDue(payment.getBill().getAmountDue())
                .totalPaid(payment.getBill().getPaidAmount())
                .outstandingBalance(payment.getBill().getOutstandingBalance())
                .fullyPaid(payment.getBill().getStatus() == BillStatus.PAID)
                .message(message)
                .receiptUrl(receiptUrl)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
