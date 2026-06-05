package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.BillLineItem;
import com.wasac.utilitybilling.dto.BillLineItemResponse;
import com.wasac.utilitybilling.dto.BillResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillMapper {
    public BillResponse toResponse(Bill bill, List<BillLineItem> lineItems) {
        return BillResponse.builder()
                .id(bill.getId())
                .billReference(bill.getBillReference())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullName())
                .customerEmail(bill.getCustomer().getEmail())
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .billingYear(bill.getBillingYear())
                .billingMonth(bill.getBillingMonth())
                .unitsConsumed(bill.getUnitsConsumed())
                .amountDue(bill.getAmountDue())
                .paidAmount(bill.getPaidAmount())
                .outstandingBalance(bill.getOutstandingBalance())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .approvalStatus(bill.getApprovalStatus())
                .lineItems(lineItems.stream().map(this::toLineItemResponse).toList())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }

    private BillLineItemResponse toLineItemResponse(BillLineItem item) {
        return BillLineItemResponse.builder()
                .id(item.getId())
                .itemType(item.getItemType())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();
    }
}
