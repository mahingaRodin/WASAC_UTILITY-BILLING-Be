package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.MeterReadingRequest;
import com.wasac.utilitybilling.dto.MeterReadingResponse;
import com.wasac.utilitybilling.service.MeterReadingService;
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
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
@Tag(name = "Meter Readings")
public class MeterReadingController {
    private final MeterReadingService meterReadingService;

    @Operation(summary = "Create meter reading")
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> create(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(ApiResponse.<MeterReadingResponse>builder()
                .success(true)
                .message("Meter reading created")
                .data(meterReadingService.create(request))
                .build());
    }

    @Operation(summary = "List meter readings")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<Page<MeterReadingResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<MeterReadingResponse>>builder()
                .success(true)
                .message("Meter readings retrieved")
                .data(meterReadingService.list(pageable))
                .build());
    }

    @Operation(summary = "Get meter reading by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<MeterReadingResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<MeterReadingResponse>builder()
                .success(true)
                .message("Meter reading retrieved")
                .data(meterReadingService.getById(id))
                .build());
    }

    @Operation(summary = "List my captured readings (operator self-service)")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public ResponseEntity<ApiResponse<Page<MeterReadingResponse>>> myReadings(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<MeterReadingResponse>>builder()
                .success(true)
                .message("My readings retrieved")
                .data(meterReadingService.getMyReadings(pageable))
                .build());
    }

    @Operation(summary = "List readings by meter")
    @GetMapping("/meter/{meterId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<List<MeterReadingResponse>>> byMeter(@PathVariable UUID meterId) {
        return ResponseEntity.ok(ApiResponse.<List<MeterReadingResponse>>builder()
                .success(true)
                .message("Meter readings retrieved")
                .data(meterReadingService.getByMeter(meterId))
                .build());
    }
}
