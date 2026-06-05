package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void sendBillGeneratedNotification(Bill bill);
    void sendFullPaymentNotification(Bill bill, Customer customer, Payment payment);
    void sendPartialPaymentNotification(Bill bill, Payment payment);
    void sendPendingPaymentNotification(Bill bill, Payment payment);
    void sendDeadlineReminder(Bill bill);
    void notifyFinanceOfPendingBill(Bill bill);
    void notifyFinanceOfPendingPayment(Payment payment);
    void sendPasswordChangedNotification(String email, String fullName);
    void broadcastNewTariff(TariffConfiguration tariff, String excludeEmail);
    void broadcastChargeChange(ChargeConfiguration charge, String excludeEmail);
    Page<NotificationResponse> list(Pageable pageable);
    List<NotificationResponse> getByCustomerId(UUID customerId);
    Page<NotificationResponse> getMyNotifications(Pageable pageable);
}
