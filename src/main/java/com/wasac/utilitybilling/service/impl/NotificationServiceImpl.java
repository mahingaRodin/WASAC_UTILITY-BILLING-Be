package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.domain.Notification;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.domain.User;
import com.wasac.utilitybilling.domain.enums.CustomerStatus;
import com.wasac.utilitybilling.domain.enums.NotificationChannel;
import com.wasac.utilitybilling.domain.enums.NotificationStatus;
import com.wasac.utilitybilling.domain.enums.NotificationType;
import com.wasac.utilitybilling.domain.enums.UserRole;
import com.wasac.utilitybilling.domain.enums.UserStatus;
import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.dto.NotificationResponse;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.mapper.BillMapper;
import com.wasac.utilitybilling.mapper.NotificationMapper;
import com.wasac.utilitybilling.repository.BillLineItemRepository;
import com.wasac.utilitybilling.repository.CustomerRepository;
import com.wasac.utilitybilling.repository.NotificationRepository;
import com.wasac.utilitybilling.repository.UserRepository;
import com.wasac.utilitybilling.service.CurrentCustomerResolver;
import com.wasac.utilitybilling.service.MailService;
import com.wasac.utilitybilling.service.NotificationService;
import com.wasac.utilitybilling.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final CurrentCustomerResolver currentCustomerResolver;
    private final NotificationMapper notificationMapper;
    private final PdfService pdfService;
    private final BillLineItemRepository billLineItemRepository;
    private final BillMapper billMapper;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendBillGeneratedNotification(Bill bill) {
        Customer customer = bill.getCustomer();
        String subject = "WASAC Bill Generated - " + bill.getBillReference();
        String message = "Dear " + customer.getFullName() + ", your bill " + bill.getBillReference()
                + " of " + bill.getAmountDue() + " FRW for " + bill.getBillingMonth() + "/" + bill.getBillingYear()
                + " has been generated and is due on " + bill.getDueDate() + ". Your bill PDF is attached.";

        notificationRepository.save(buildNotification(customer, bill, NotificationType.BILL_GENERATED, subject, message));

        try {
            BillResponse billResponse = billMapper.toResponse(bill, billLineItemRepository.findByBill_Id(bill.getId()));
            byte[] pdf = pdfService.generateBillPdf(billResponse);
            mailService.sendTemplateEmailWithAttachment(customer.getEmail(), "account-notification", Map.of(
                            "title", "Bill Generated",
                            "userName", customer.getFullName(),
                            "message", message),
                    "bill-" + bill.getBillReference() + ".pdf",
                    pdf);
        } catch (Exception ex) {
            log.error("Best-effort bill notification email failed for bill {}", bill.getBillReference(), ex);
        }
    }

    @Override
    @Transactional
    public void sendFullPaymentNotification(Bill bill, Customer customer, Payment payment) {
        String subject = "Payment Received - " + bill.getBillReference();
        String message = "Dear " + customer.getFullName() + ",\nYour " + bill.getBillingMonth() + "/" + bill.getBillingYear()
                + " utility bill of " + bill.getAmountDue() + " FRW has been successfully processed.";

        notificationRepository.save(buildNotification(customer, bill, NotificationType.PAYMENT_COMPLETED, subject, message));

        try {
            BillResponse billResponse = billMapper.toResponse(bill, billLineItemRepository.findByBill_Id(bill.getId()));
            PaymentResponse paymentResponse = PaymentResponse.builder()
                    .id(payment.getId())
                    .billReference(bill.getBillReference())
                    .amountPaid(bill.getAmountDue())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentDate(payment.getPaymentDate())
                    .build();
            byte[] receipt = pdfService.generateReceiptPdf(billResponse, paymentResponse);
            mailService.sendTemplateEmailWithAttachment(customer.getEmail(), "account-notification", Map.of(
                            "title", "Payment Completed",
                            "userName", customer.getFullName(),
                            "message", message),
                    "receipt-" + bill.getBillReference() + ".pdf",
                    receipt);
        } catch (Exception ex) {
            log.error("Best-effort full payment email failed for bill {}", bill.getBillReference(), ex);
        }
    }

    @Override
    @Transactional
    public void sendPendingPaymentNotification(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String message = "Dear " + customer.getFullName() + ", your payment of " + payment.getAmountPaid()
                + " FRW for bill " + bill.getBillReference() + " has been received and is pending finance approval. "
                + "You will receive another notification once it is approved or rejected.";

        notificationRepository.save(buildNotification(customer, bill, NotificationType.PAYMENT_PENDING, "Payment Submitted", message));

        try {
            mailService.sendTemplateEmail(customer.getEmail(), "account-notification", Map.of(
                    "title", "Payment Submitted",
                    "userName", customer.getFullName(),
                    "message", message
            ));
        } catch (Exception ex) {
            log.error("Best-effort pending payment email failed for bill {}", bill.getBillReference(), ex);
        }
    }

    @Override
    @Transactional
    public void sendPartialPaymentNotification(Bill bill, Payment payment) {
        Customer customer = bill.getCustomer();
        String message = "Dear " + customer.getFullName() + ", we received your payment of " + payment.getAmountPaid()
                + " FRW for bill " + bill.getBillReference() + ". Remaining balance: " + bill.getOutstandingBalance() + " FRW.";

        notificationRepository.save(buildNotification(customer, bill, NotificationType.PARTIAL_PAYMENT, "Payment Received", message));

        try {
            mailService.sendTemplateEmail(customer.getEmail(), "account-notification", Map.of(
                    "title", "Payment Received",
                    "userName", customer.getFullName(),
                    "message", message
            ));
        } catch (Exception ex) {
            log.error("Best-effort partial payment email failed for bill {}", bill.getBillReference(), ex);
        }
    }

    @Override
    @Transactional
    public void sendDeadlineReminder(Bill bill) {
        Customer customer = bill.getCustomer();
        String message = "Dear " + customer.getFullName() + ", your bill " + bill.getBillReference() + " of "
                + bill.getOutstandingBalance() + " FRW is due on " + bill.getDueDate()
                + ". Please settle it to avoid late-payment penalties.";

        notificationRepository.save(buildNotification(customer, bill, NotificationType.DEADLINE_REMINDER, "Payment Reminder", message));

        try {
            mailService.sendTemplateEmail(customer.getEmail(), "account-notification", Map.of(
                    "title", "Payment Reminder",
                    "userName", customer.getFullName(),
                    "message", message
            ));
        } catch (Exception ex) {
            log.error("Best-effort deadline reminder email failed for bill {}", bill.getBillReference(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyFinanceOfPendingBill(Bill bill) {
        Customer customer = bill.getCustomer();
        String message = "A new bill " + bill.getBillReference() + " of " + bill.getAmountDue()
                + " FRW for customer " + customer.getFullName() + " (" + bill.getBillingMonth() + "/" + bill.getBillingYear()
                + ") is awaiting your approval. The customer will only be notified once you approve it.";
        emailFinanceTeam("Bill Awaiting Approval", message);
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyFinanceOfPendingPayment(Payment payment) {
        Bill bill = payment.getBill();
        String message = "A payment of " + payment.getAmountPaid() + " FRW for bill " + bill.getBillReference()
                + " (customer " + bill.getCustomer().getFullName() + ") is awaiting your approval. "
                + "The customer has been notified that their payment is pending approval.";
        emailFinanceTeam("Payment Awaiting Approval", message);
    }

    @Override
    @Transactional
    public void sendPasswordChangedNotification(String email, String fullName) {
        String message = "Your password has been updated successfully. If you did not perform this action, contact support immediately.";
        try {
            mailService.sendTemplateEmail(email, "account-notification", Map.of(
                    "title", "Password Updated",
                    "userName", fullName,
                    "message", message
            ));
        } catch (Exception ex) {
            log.error("Best-effort password update email failed for {}", email, ex);
        }
    }

    @Override
    @Transactional
    public void broadcastNewTariff(TariffConfiguration tariff, String excludeEmail) {
        List<Customer> activeCustomers = customerRepository.findByStatus(CustomerStatus.ACTIVE);
        String message = "A new " + tariff.getUtilityType() + " tariff (version " + tariff.getVersion()
                + ", effective from " + tariff.getEffectiveFrom()
                + ") has been published and applies to future billing cycles.";

        activeCustomers.forEach(customer -> notificationRepository.save(buildNotification(
                customer, null, NotificationType.TARIFF_UPDATE, "New Tariff Published", message)));

        broadcastEmailToAudience(
                activeCustomers,
                userRepository.findAll(),
                excludeEmail,
                "New Tariff Published",
                message
        );
    }

    @Override
    @Transactional
    public void broadcastChargeChange(ChargeConfiguration charge, String excludeEmail) {
        List<Customer> activeCustomers = customerRepository.findByStatus(CustomerStatus.ACTIVE);
        String message = "An updated " + charge.getChargeType() + " charge (version " + charge.getVersion()
                + ", effective from " + charge.getEffectiveFrom() + ") has been configured.";

        activeCustomers.forEach(customer -> notificationRepository.save(buildNotification(
                customer, null, NotificationType.CHARGE_UPDATE, "Charge Configuration Updated", message)));

        broadcastEmailToAudience(
                activeCustomers,
                userRepository.findAll(),
                excludeEmail,
                "Charge Configuration Updated",
                message
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> list(Pageable pageable) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable).map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByCustomerId(UUID customerId) {
        return notificationRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        String email = currentCustomerResolver.resolve().getEmail();
        return notificationRepository.findByCustomer_EmailOrderByCreatedAtDesc(email, pageable)
                .map(notificationMapper::toResponse);
    }

    private Notification buildNotification(
            Customer customer,
            Bill bill,
            NotificationType type,
            String subject,
            String message
    ) {
        return Notification.builder()
                .customer(customer)
                .bill(bill)
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();
    }

    private void broadcastEmailToAudience(
            List<Customer> customers,
            List<User> users,
            String excludeEmail,
            String title,
            String message
    ) {
        String normalizedExclude = normalizeEmail(excludeEmail);
        Set<String> seen = new HashSet<>();
        Map<String, String> recipients = new HashMap<>();

        for (Customer customer : customers) {
            registerRecipient(recipients, seen, customer.getEmail(), customer.getFullName(), normalizedExclude);
        }
        for (User user : users) {
            registerRecipient(recipients, seen, user.getEmail(), user.getFullName(), normalizedExclude);
        }

        recipients.forEach((email, name) -> {
            try {
                mailService.sendTemplateEmail(email, "account-notification", Map.of(
                        "title", title,
                        "userName", name != null ? name : "Valued Customer",
                        "message", message
                ));
            } catch (Exception ex) {
                log.error("Best-effort broadcast email failed for {}", email, ex);
            }
        });
    }

    private void emailFinanceTeam(String title, String message) {
        List<User> financeUsers = userRepository.findByRoleAndStatus(UserRole.ROLE_FINANCE, UserStatus.ACTIVE);
        for (User financeUser : financeUsers) {
            try {
                mailService.sendTemplateEmail(financeUser.getEmail(), "account-notification", Map.of(
                        "title", title,
                        "userName", financeUser.getFullName(),
                        "message", message
                ));
            } catch (Exception ex) {
                log.error("Best-effort finance notification email failed for {}", financeUser.getEmail(), ex);
            }
        }
    }

    private void registerRecipient(
            Map<String, String> recipients,
            Set<String> seen,
            String email,
            String fullName,
            String normalizedExclude
    ) {
        String normalized = normalizeEmail(email);
        if (normalized == null || normalized.equals(normalizedExclude) || !seen.add(normalized)) {
            return;
        }
        recipients.put(normalized, fullName);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
