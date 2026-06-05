package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.Payment;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> process(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.<Payment>builder()
                .success(true)
                .message("Payment processed")
                .data(paymentService.process(request))
                .build());
    }
}
