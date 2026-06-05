package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.domain.enums.PaymentMethod;
import com.wasac.utilitybilling.domain.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID billId;
    private String billReference;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private PaymentStatus status;
    private BillStatus billStatus;
    private BigDecimal amountDue;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;
    private boolean fullyPaid;
    private String message;
    private String receiptUrl;
    private LocalDateTime createdAt;
}
