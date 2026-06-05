package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.MeterRequest;
import com.wasac.utilitybilling.dto.MeterResponse;
import com.wasac.utilitybilling.dto.UpdateMeterStatusRequest;
import com.wasac.utilitybilling.service.MeterService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "Meters")
public class MeterController {
    private final MeterService meterService;

    @Operation(summary = "Create meter")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<MeterResponse>> create(@Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter created")
                .data(meterService.create(request))
                .build());
    }

    @Operation(summary = "List meters")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<Page<MeterResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<MeterResponse>>builder()
                .success(true)
                .message("Meters retrieved")
                .data(meterService.list(pageable))
                .build());
    }

    @Operation(summary = "Get meter by id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<MeterResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter retrieved")
                .data(meterService.getById(id))
                .build());
    }

    @Operation(summary = "List customer meters")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<Page<MeterResponse>>> byCustomer(@PathVariable UUID customerId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<MeterResponse>>builder()
                .success(true)
                .message("Customer meters retrieved")
                .data(meterService.getByCustomerId(customerId, pageable))
                .build());
    }

    @Operation(summary = "Update meter status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    public ResponseEntity<ApiResponse<MeterResponse>> updateStatus(@PathVariable UUID id,
                                                                   @Valid @RequestBody UpdateMeterStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.<MeterResponse>builder()
                .success(true)
                .message("Meter status updated")
                .data(meterService.updateStatus(id, request))
                .build());
    }

    @Operation(summary = "Delete meter")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        meterService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Meter deleted")
                .build());
    }

    @Operation(summary = "List my meters")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> myMeters() {
        return ResponseEntity.ok(ApiResponse.<List<MeterResponse>>builder()
                .success(true)
                .message("My meters retrieved")
                .data(meterService.getMyMeters())
                .build());
    }
}
