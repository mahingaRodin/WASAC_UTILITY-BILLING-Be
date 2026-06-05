package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.BillApprovalStatus;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class BillResponse {
    private UUID id;
    private String billReference;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID meterId;
    private String meterNumber;
    private Integer billingYear;
    private Integer billingMonth;
    private BigDecimal unitsConsumed;
    private BigDecimal amountDue;
    private BigDecimal paidAmount;
    private BigDecimal outstandingBalance;
    private LocalDate dueDate;
    private BillStatus status;
    private BillApprovalStatus approvalStatus;
    private List<BillLineItemResponse> lineItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
