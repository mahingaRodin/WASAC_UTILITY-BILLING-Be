package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.Customer;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.CustomerRequest;
import com.wasac.utilitybilling.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<Customer>> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.<Customer>builder()
                .success(true)
                .message("Customer created")
                .data(customerService.create(request))
                .build());
    }
}
