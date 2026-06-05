package com.wasac.utilitybilling.scheduler;

import com.wasac.utilitybilling.domain.enums.BillStatus;
import com.wasac.utilitybilling.repository.BillRepository;
import com.wasac.utilitybilling.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillReminderScheduler {
    private final BillRepository billRepository;
    private final NotificationService notificationService;

    @Value("${app.reminders.enabled:true}")
    private boolean enabled;

    @Value("${app.reminders.lead-days:3}")
    private long leadDays;

    @Scheduled(cron = "${app.reminders.cron:0 0 8 * * *}")
    @Transactional
    public void sendDueReminders() {
        if (!enabled) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(leadDays);
        billRepository.findByDueDateBetweenAndOutstandingBalanceGreaterThan(today, horizon, BigDecimal.ZERO).stream()
                .filter(bill -> bill.getStatus() != BillStatus.PAID)
                .forEach(notificationService::sendDeadlineReminder);
    }
}
