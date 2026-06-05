package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.dto.CustomerResponse;
import com.wasac.utilitybilling.dto.UpdateCustomerRequest;
import com.wasac.utilitybilling.dto.UpdateCustomerStatusRequest;
import com.wasac.utilitybilling.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomerController {
    private final CustomerService customerService;

    @Operation(summary = "Create customer")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer created")
                .data(customerService.create(request))
                .build());
    }

    @Operation(summary = "List customers")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<CustomerResponse>>builder()
                .success(true)
                .message("Customers retrieved")
                .data(customerService.list(pageable))
                .build());
    }

    @Operation(summary = "Get customer by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer retrieved")
                .data(customerService.getById(id))
                .build());
    }

    @Operation(summary = "Update customer")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(@PathVariable java.util.UUID id,
                                                                @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer updated")
                .data(customerService.update(id, request))
                .build());
    }

    @Operation(summary = "Update customer status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(@PathVariable java.util.UUID id,
                                                                      @Valid @RequestBody UpdateCustomerStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer status updated")
                .data(customerService.updateStatus(id, request))
                .build());
    }

    @Operation(summary = "Delete customer")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable java.util.UUID id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Customer deleted")
                .build());
    }

    @Operation(summary = "Get my customer profile")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerResponse>> myProfile() {
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .message("Customer profile retrieved")
                .data(customerService.getMyProfile())
                .build());
    }
}
