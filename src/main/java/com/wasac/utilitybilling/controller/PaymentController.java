package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.PaymentRequest;
import com.wasac.utilitybilling.dto.PaymentResponse;
import com.wasac.utilitybilling.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "Submit a payment (pending finance approval)")
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> process(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment submitted")
                .data(paymentService.process(request))
                .build());
    }

    @Operation(summary = "List payments pending finance approval")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> pending(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true)
                .message("Pending payments retrieved")
                .data(paymentService.listPendingApproval(pageable))
                .build());
    }

    @Operation(summary = "Approve a payment (updates the bill and notifies the customer)")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment approved")
                .data(paymentService.approve(id))
                .build());
    }

    @Operation(summary = "Reject a payment")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment rejected")
                .data(paymentService.reject(id))
                .build());
    }

    @Operation(summary = "List payments")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true)
                .message("Payments retrieved")
                .data(paymentService.list(pageable))
                .build());
    }

    @Operation(summary = "Get payment by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<PaymentResponse>builder()
                .success(true)
                .message("Payment retrieved")
                .data(paymentService.getById(id))
                .build());
    }

    @Operation(summary = "Get payments by bill reference")
    @GetMapping("/bill/{billReference}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> byBill(@PathVariable String billReference) {
        return ResponseEntity.ok(ApiResponse.<List<PaymentResponse>>builder()
                .success(true)
                .message("Bill payments retrieved")
                .data(paymentService.getByBillReference(billReference))
                .build());
    }

    @Operation(summary = "Get my payments")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> myPayments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<PaymentResponse>>builder()
                .success(true)
                .message("My payments retrieved")
                .data(paymentService.getMyPayments(pageable))
                .build());
    }
}
