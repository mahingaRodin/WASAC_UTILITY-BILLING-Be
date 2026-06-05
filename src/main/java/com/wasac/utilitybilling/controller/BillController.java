package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.BillRequest;
import com.wasac.utilitybilling.dto.BillResponse;
import com.wasac.utilitybilling.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "Bills")
public class BillController {
    private final BillService billService;

    @Operation(summary = "Create bill")
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<BillResponse>> create(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill created")
                .data(billService.create(request))
                .build());
    }

    @Operation(summary = "List bills")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<Page<BillResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("Bills retrieved")
                .data(billService.list(pageable))
                .build());
    }

    @Operation(summary = "List bills pending finance approval")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<Page<BillResponse>>> pending(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("Pending bills retrieved")
                .data(billService.listPendingApproval(pageable))
                .build());
    }

    @Operation(summary = "Approve a bill (notifies the customer)")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<BillResponse>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill approved")
                .data(billService.approve(id))
                .build());
    }

    @Operation(summary = "Reject a bill")
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('FINANCE','ADMIN')")
    public ResponseEntity<ApiResponse<BillResponse>> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill rejected")
                .data(billService.reject(id))
                .build());
    }

    @Operation(summary = "Get bill by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<BillResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill retrieved")
                .data(billService.getById(id))
                .build());
    }

    @Operation(summary = "Get bill by reference")
    @GetMapping("/reference/{ref}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<BillResponse>> getByRef(@PathVariable String ref) {
        return ResponseEntity.ok(ApiResponse.<BillResponse>builder()
                .success(true)
                .message("Bill retrieved")
                .data(billService.getByReference(ref))
                .build());
    }

    @Operation(summary = "Get bills by customer")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<Page<BillResponse>>> byCustomer(@PathVariable UUID customerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("Customer bills retrieved")
                .data(billService.getByCustomerId(customerId, pageable))
                .build());
    }

    @Operation(summary = "Get my bills")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<BillResponse>>> myBills(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<BillResponse>>builder()
                .success(true)
                .message("My bills retrieved")
                .data(billService.getMyBills(pageable))
                .build());
    }

    @Operation(summary = "Download bill PDF")
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    public ResponseEntity<byte[]> downloadBillPdf(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"bill-" + id + ".pdf\"")
                .body(billService.generateBillPdf(id));
    }

    @Operation(summary = "Download payment receipt PDF")
    @GetMapping(value = "/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE','CUSTOMER')")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable UUID id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"receipt-" + id + ".pdf\"")
                .body(billService.generateReceiptPdf(id));
    }
}
