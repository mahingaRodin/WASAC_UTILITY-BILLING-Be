package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.Notification;
import com.wasac.utilitybilling.dto.NotificationResponse;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer().getId())
                .customerName(notification.getCustomer().getFullName())
                .customerEmail(notification.getCustomer().getEmail())
                .billId(notification.getBill() != null ? notification.getBill().getId() : null)
                .billReference(notification.getBill() != null ? notification.getBill().getBillReference() : null)
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
