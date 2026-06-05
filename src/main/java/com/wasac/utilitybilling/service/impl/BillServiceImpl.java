package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.BillLineItem;
import com.wasac.utilitybilling.dto.BillRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.repository.BillLineItemRepository;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.service.BillService;
import com.wasac.utilitybilling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillLineItemRepository billLineItemRepository;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Bill create(BillRequest request) {
        if (billRepository.findByBillReference(request.getBillReference()).isPresent()) {
            throw new BadRequestException("Bill reference already exists.");
        }

        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        var meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));

        Bill bill = billRepository.save(Bill.builder()
                .billReference(request.getBillReference())
                .customer(customer)
                .meter(meter)
                .billingYear(request.getBillingYear())
                .billingMonth(request.getBillingMonth())
                .unitsConsumed(request.getUnitsConsumed())
                .amountDue(request.getAmountDue())
                .paidAmount(BigDecimal.ZERO)
                .outstandingBalance(request.getAmountDue())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .build());

        if (request.getLineItems() != null) {
            for (var item : request.getLineItems()) {
                billLineItemRepository.save(BillLineItem.builder()
                        .bill(bill)
                        .itemType(item.getItemType())
                        .description(item.getDescription())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getUnitPrice().multiply(item.getQuantity()))
                        .build());
            }
        }
        notificationService.sendBillGeneratedNotification(bill);
        return bill;
    }
}
