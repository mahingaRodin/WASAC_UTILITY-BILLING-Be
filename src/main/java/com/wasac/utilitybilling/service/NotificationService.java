package com.wasac.utilitybilling.service;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.domain.Customer;

public interface NotificationService {
    void sendBillGeneratedNotification(Bill bill);
    void sendFullPaymentNotification(Bill bill, Customer customer);
}
