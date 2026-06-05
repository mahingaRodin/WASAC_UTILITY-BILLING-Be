package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.domain.enums.PaymentMethod;
import com.wasac.utilitybilling.dto.BillLineItemResponse;
import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.domain.enums.BillLineItemType;
import com.wasac.utilitybilling.service.impl.PdfServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    private final PdfService pdfService = new PdfServiceImpl();

    private BillResponse sampleBill() {
        return BillResponse.builder()
                .id(UUID.randomUUID())
                .billReference("BILL-2026-01")
                .customerName("Alice Uwase")
                .customerEmail("alice@wasac.rw")
                .meterNumber("MTR-0001")
                .billingYear(2026)
                .billingMonth(1)
                .unitsConsumed(new BigDecimal("12.5"))
                .amountDue(new BigDecimal("5000"))
                .paidAmount(new BigDecimal("5000"))
                .outstandingBalance(BigDecimal.ZERO)
                .dueDate(LocalDate.of(2026, 2, 15))
                .status(BillStatus.PAID)
                .createdAt(LocalDateTime.of(2026, 1, 1, 9, 0))
                .lineItems(List.of(BillLineItemResponse.builder()
                        .id(UUID.randomUUID())
                        .itemType(BillLineItemType.CONSUMPTION)
                        .description("Water usage")
                        .quantity(new BigDecimal("12.5"))
                        .unitPrice(new BigDecimal("400"))
                        .amount(new BigDecimal("5000"))
                        .build()))
                .build();
    }

    @Test
    void generatesValidBillPdf() {
        byte[] pdf = pdfService.generateBillPdf(sampleBill());
        assertTrue(pdf.length > 0, "PDF should not be empty");
        assertTrue(startsWithPdfMagic(pdf), "Output should be a valid PDF document");
    }

    @Test
    void generatesValidBillPdfWithNoLineItems() {
        BillResponse bill = sampleBill();
        BillResponse noItems = BillResponse.builder()
                .id(bill.getId())
                .billReference(bill.getBillReference())
                .customerName(bill.getCustomerName())
                .customerEmail(bill.getCustomerEmail())
                .meterNumber(bill.getMeterNumber())
                .billingYear(bill.getBillingYear())
                .billingMonth(bill.getBillingMonth())
                .amountDue(bill.getAmountDue())
                .paidAmount(bill.getPaidAmount())
                .outstandingBalance(bill.getOutstandingBalance())
                .dueDate(bill.getDueDate())
                .status(bill.getStatus())
                .createdAt(bill.getCreatedAt())
                .lineItems(List.of())
                .build();
        byte[] pdf = pdfService.generateBillPdf(noItems);
        assertTrue(startsWithPdfMagic(pdf), "Output should be a valid PDF document");
    }

    @Test
    void generatesValidReceiptPdf() {
        PaymentResponse payment = PaymentResponse.builder()
                .id(UUID.randomUUID())
                .billReference("BILL-2026-01")
                .amountPaid(new BigDecimal("5000"))
                .paymentMethod(PaymentMethod.MOBILE_MONEY)
                .paymentDate(LocalDate.of(2026, 2, 10))
                .build();
        byte[] pdf = pdfService.generateReceiptPdf(sampleBill(), payment);
        assertTrue(startsWithPdfMagic(pdf), "Output should be a valid PDF document");
    }

    private boolean startsWithPdfMagic(byte[] bytes) {
        return bytes.length >= 4
                && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }
}
