package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.service.MeterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
public class MeterController {
    private final MeterService meterService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<Meter>> create(@Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(ApiResponse.<Meter>builder()
                .success(true)
                .message("Meter created")
                .data(meterService.create(request))
                .build());
    }
}
