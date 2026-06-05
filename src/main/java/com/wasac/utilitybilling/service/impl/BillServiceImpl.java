package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.BillLineItem;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.BillApprovalStatus;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.domain.enums.PaymentStatus;
import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.dto.BillRequest;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.BillMapper;
import com.wasac.utilitybilling.repository.BillLineItemRepository;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.MeterRepository;
import com.wasac.utilitybilling.repository.PaymentRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import com.wasac.utilitybilling.service.BillService;
import com.wasac.utilitybilling.service.NotificationService;
import com.wasac.utilitybilling.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillLineItemRepository billLineItemRepository;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final NotificationService notificationService;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final BillMapper billMapper;
    private final PdfService pdfService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public BillResponse create(BillRequest request) {
        if (billRepository.findByBillReference(request.getBillReference()).isPresent()) {
            throw new BadRequestException("Bill reference already exists.");
        }
        if (request.getAmountDue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount due must be greater than zero.");
        }
        if (request.getBillingMonth() < 1 || request.getBillingMonth() > 12) {
            throw new BadRequestException("Billing month must be between 1 and 12.");
        }
        LocalDate billingPeriod = LocalDate.of(request.getBillingYear(), request.getBillingMonth(), 1);
        if (request.getDueDate().isBefore(billingPeriod)) {
            throw new BadRequestException("Due date cannot be before billing period.");
        }

        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        var meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found."));
        if (!meter.getCustomer().getId().equals(request.getCustomerId())) {
            throw new BadRequestException("Meter does not belong to the provided customer.");
        }

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
                .status(BillStatus.UNPAID)
                .approvalStatus(BillApprovalStatus.PENDING)
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
        notificationService.notifyFinanceOfPendingBill(bill);
        return billMapper.toResponse(bill, billLineItemRepository.findByBill_Id(bill.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> list(Pageable pageable) {
        return billRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponseWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> listPendingApproval(Pageable pageable) {
        return billRepository.findByApprovalStatusOrderByCreatedAtDesc(BillApprovalStatus.PENDING, pageable)
                .map(this::toResponseWithItems);
    }

    @Override
    @Transactional
    public BillResponse approve(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        if (bill.getApprovalStatus() != BillApprovalStatus.PENDING) {
            throw new BadRequestException("Only pending bills can be approved.");
        }
        bill.setApprovalStatus(BillApprovalStatus.APPROVED);
        Bill saved = billRepository.save(bill);
        notificationService.sendBillGeneratedNotification(saved);
        return toResponseWithItems(saved);
    }

    @Override
    @Transactional
    public BillResponse reject(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        if (bill.getApprovalStatus() != BillApprovalStatus.PENDING) {
            throw new BadRequestException("Only pending bills can be rejected.");
        }
        bill.setApprovalStatus(BillApprovalStatus.REJECTED);
        return toResponseWithItems(billRepository.save(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getById(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        return toResponseWithItems(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getByReference(String billReference) {
        Bill bill = billRepository.findByBillReference(billReference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        return toResponseWithItems(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> getByCustomerId(UUID customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found.");
        }
        return billRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toResponseWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillResponse> getMyBills(Pageable pageable) {
        String email = currentCustomerResolver.resolve().getEmail();
        return billRepository.findByCustomer_EmailOrderByCreatedAtDesc(email, pageable)
                .map(this::toResponseWithItems);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateBillPdf(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        verifyOwnershipForCustomerOnlyUser(bill);
        return pdfService.generateBillPdf(toResponseWithItems(bill));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateReceiptPdf(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));
        verifyOwnershipForCustomerOnlyUser(bill);
        if (bill.getStatus() != BillStatus.PAID) {
            throw new BadRequestException("Receipt is only available for fully paid bills.");
        }

        BillResponse billResponse = toResponseWithItems(bill);
        Payment latestApproved = paymentRepository
                .findTopByBill_IdAndStatusOrderByPaymentDateDesc(bill.getId(), PaymentStatus.APPROVED)
                .orElse(null);
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .id(latestApproved != null ? latestApproved.getId() : null)
                .billReference(bill.getBillReference())
                .amountPaid(bill.getAmountDue())
                .paymentMethod(latestApproved != null ? latestApproved.getPaymentMethod() : null)
                .paymentDate(latestApproved != null ? latestApproved.getPaymentDate() : LocalDate.now())
                .build();
        return pdfService.generateReceiptPdf(billResponse, paymentResponse);
    }

    private BillResponse toResponseWithItems(Bill bill) {
        return billMapper.toResponse(bill, billLineItemRepository.findByBill_Id(bill.getId()));
    }

    private void verifyOwnershipForCustomerOnlyUser(Bill bill) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));
        boolean hasPrivilegedRole = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_OPERATOR".equals(a.getAuthority())
                        || "ROLE_FINANCE".equals(a.getAuthority()));
        if (isCustomer && !hasPrivilegedRole
                && !bill.getCustomer().getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("You can only access your own bills.");
        }
    }
}
