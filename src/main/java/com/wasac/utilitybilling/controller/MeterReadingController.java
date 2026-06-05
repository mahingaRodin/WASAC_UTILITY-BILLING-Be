package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.MeterReading;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.service.MeterReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController {
    private final MeterReadingService meterReadingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<MeterReading>> create(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.<MeterReading>builder()
                .success(true)
                .message("Meter reading created")
                .data(meterReadingService.create(request))
                .build());
    }
}
