package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.enums.BillApprovalStatus;
import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.domain.enums.PaymentStatus;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.exception.BadRequestException;
import com.wasac.utilitybilling.exception.ResourceNotFoundException;
import com.wasac.utilitybilling.mapper.PaymentMapper;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.repository.PaymentRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import com.wasac.utilitybilling.service.NotificationService;
import com.wasac.utilitybilling.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse process(PaymentRequest request) {
        Bill bill = billRepository.findByBillReference(request.getBillReference())
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found."));

        verifyBillOwnershipForCustomerOnlyUser(bill);

        if (bill.getApprovalStatus() != BillApprovalStatus.APPROVED) {
            throw new BadRequestException("Bill has not been approved by finance yet.");
        }
        if (request.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be positive.");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill already fully paid");
        }

        BigDecimal pendingTotal = pendingPaymentTotal(bill.getId());
        BigDecimal available = bill.getOutstandingBalance().subtract(pendingTotal);
        if (request.getAmountPaid().compareTo(available) > 0) {
            throw new BadRequestException(
                    "Payment exceeds the amount still payable (outstanding minus pending submissions): " + available + " FRW.");
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .bill(bill)
                .billReference(bill.getBillReference())
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .status(PaymentStatus.PENDING)
                .build());

        notificationService.notifyFinanceOfPendingPayment(payment);
        notificationService.sendPendingPaymentNotification(bill, payment);

        String message = "Payment of " + payment.getAmountPaid() + " FRW for bill " + bill.getBillReference()
                + " submitted and is pending finance approval. You will be notified once it is approved.";
        return paymentMapper.toResponse(payment, message, null);
    }

    @Override
    @Transactional
    public PaymentResponse approve(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only pending payments can be approved.");
        }

        Bill bill = payment.getBill();
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill already fully paid.");
        }
        if (payment.getAmountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BadRequestException("Payment exceeds the current outstanding balance.");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepository.save(payment);

        bill.setPaidAmount(bill.getPaidAmount().add(payment.getAmountPaid()));
        BigDecimal outstanding = bill.getAmountDue().subtract(bill.getPaidAmount());
        bill.setOutstandingBalance(outstanding.max(BigDecimal.ZERO));
        bill.setStatus(bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0
                ? BillStatus.PAID
                : BillStatus.PARTIALLY_PAID);
        Bill savedBill = billRepository.save(bill);

        String message;
        String receiptUrl;
        if (savedBill.getStatus() == BillStatus.PAID) {
            notificationService.sendFullPaymentNotification(savedBill, savedBill.getCustomer(), payment);
            message = "Dear " + savedBill.getCustomer().getFullName() + ",\nYour " + savedBill.getBillingMonth() + "/"
                    + savedBill.getBillingYear() + " utility bill of " + savedBill.getAmountDue()
                    + " FRW has been successfully processed.";
            receiptUrl = "/api/bills/" + savedBill.getId() + "/receipt";
        } else {
            notificationService.sendPartialPaymentNotification(savedBill, payment);
            message = "Payment of " + payment.getAmountPaid() + " FRW approved. Remaining balance: "
                    + savedBill.getOutstandingBalance() + " FRW.";
            receiptUrl = null;
        }
        return paymentMapper.toResponse(payment, message, receiptUrl);
    }

    @Override
    @Transactional
    public PaymentResponse reject(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Only pending payments can be rejected.");
        }
        payment.setStatus(PaymentStatus.REJECTED);
        paymentRepository.save(payment);
        return paymentMapper.toResponse(payment, "Payment was rejected by finance.", null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> listPendingApproval(Pageable pageable) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING, pageable)
                .map(paymentMapper::toResponse);
    }

    private BigDecimal pendingPaymentTotal(UUID billId) {
        return paymentRepository.findByBill_IdAndStatus(billId, PaymentStatus.PENDING).stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void verifyBillOwnershipForCustomerOnlyUser(Bill bill) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean isCustomer = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));
        boolean hasPrivilegedRole = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ROLE_FINANCE".equals(a.getAuthority())
                        || "ROLE_OPERATOR".equals(a.getAuthority()));
        if (isCustomer && !hasPrivilegedRole
                && !bill.getCustomer().getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("You can only pay your own bills.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> list(Pageable pageable) {
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable).map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByBillReference(String billReference) {
        return paymentRepository.findByBillReferenceOrderByCreatedAtDesc(billReference).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(Pageable pageable) {
        String email = currentCustomerResolver.resolve().getEmail();
        return paymentRepository.findByBill_Customer_EmailOrderByCreatedAtDesc(email, pageable)
                .map(paymentMapper::toResponse);
    }
}
