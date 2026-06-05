package com.wasac.utilitybilling.service.impl;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.domain.Notification;
import com.wasac.utilitybilling.domain.enums.NotificationChannel;
import com.wasac.utilitybilling.domain.enums.NotificationStatus;
import com.wasac.utilitybilling.domain.enums.NotificationType;
import com.wasac.utilitybilling.repository.NotificationRepository;
import com.wasac.utilitybilling.service.MailService;
import com.wasac.utilitybilling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public void sendBillGeneratedNotification(Bill bill) {
        Customer customer = bill.getCustomer();
        String subject = "WASAC Bill Generated - " + bill.getBillReference();
        String message = String.format(
                "Dear %s, your bill %s for amount %s has been generated and is due on %s.",
                customer.getFullName(), bill.getBillReference(), bill.getAmountDue(), bill.getDueDate());

        notificationRepository.save(Notification.builder()
                .customer(customer)
                .bill(bill)
                .type(NotificationType.BILL_GENERATED)
                .channel(NotificationChannel.EMAIL)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build());

        mailService.sendTemplateEmail(customer.getEmail(), "account-notification", Map.of(
                "title", "Bill Generated",
                "userName", customer.getFullName(),
                "message", message
        ));
    }

    @Override
    @Transactional
    public void sendFullPaymentNotification(Bill bill, Customer customer) {
        String subject = "Payment Received - " + bill.getBillReference();
        String message = String.format(
                "Dear %s, your full payment for bill %s has been received. Thank you.",
                customer.getFullName(), bill.getBillReference());

        notificationRepository.save(Notification.builder()
                .customer(customer)
                .bill(bill)
                .type(NotificationType.PAYMENT_COMPLETED)
                .channel(NotificationChannel.EMAIL)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build());

        mailService.sendTemplateEmail(customer.getEmail(), "account-notification", Map.of(
                "title", "Payment Completed",
                "userName", customer.getFullName(),
                "message", message
        ));
    }
}
