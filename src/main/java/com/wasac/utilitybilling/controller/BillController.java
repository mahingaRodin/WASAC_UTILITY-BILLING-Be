package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.Bill;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.BillRequest;
import com.wasac.utilitybilling.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {
    private final BillService billService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Bill>> create(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(ApiResponse.<Bill>builder()
                .success(true)
                .message("Bill created")
                .data(billService.create(request))
                .build());
    }
}
