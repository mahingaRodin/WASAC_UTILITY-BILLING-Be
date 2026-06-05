package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.NotificationResponse;
import com.wasac.utilitybilling.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "List notifications")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved")
                .data(notificationService.list(pageable))
                .build());
    }

    @Operation(summary = "List notifications by customer")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> byCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .success(true)
                .message("Customer notifications retrieved")
                .data(notificationService.getByCustomerId(customerId))
                .build());
    }

    @Operation(summary = "List my notifications")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> myNotifications(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("My notifications retrieved")
                .data(notificationService.getMyNotifications(pageable))
                .build());
    }
}
