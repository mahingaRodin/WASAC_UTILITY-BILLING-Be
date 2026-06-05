package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.NotificationChannel;
import com.wasac.utilitybilling.domain.enums.NotificationStatus;
import com.wasac.utilitybilling.domain.enums.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private UUID billId;
    private String billReference;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
