package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.ChargeResponse;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.TariffResponse;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "Configuration")
public class TariffController {
    private final TariffService tariffService;

    @Operation(summary = "Create tariff")
    @PostMapping("/tariffs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(@Valid @RequestBody TariffConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff configuration created")
                .data(tariffService.createTariff(request))
                .build());
    }

    @Operation(summary = "List tariffs")
    @GetMapping("/tariffs")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<List<TariffResponse>>> listTariffs() {
        return ResponseEntity.ok(ApiResponse.<List<TariffResponse>>builder()
                .success(true)
                .message("Tariffs retrieved")
                .data(tariffService.listTariffs())
                .build());
    }

    @Operation(summary = "Get tariff by id")
    @GetMapping("/tariffs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariff(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff retrieved")
                .data(tariffService.getTariffById(id))
                .build());
    }

    @Operation(summary = "Deactivate tariff")
    @PatchMapping("/tariffs/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffResponse>> deactivateTariff(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<TariffResponse>builder()
                .success(true)
                .message("Tariff deactivated")
                .data(tariffService.deactivateTariff(id))
                .build());
    }

    @Operation(summary = "Create charge")
    @PostMapping("/charges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChargeResponse>> createCharge(@Valid @RequestBody ChargeConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChargeResponse>builder()
                .success(true)
                .message("Charge configuration created")
                .data(tariffService.createCharge(request))
                .build());
    }

    @Operation(summary = "List charges")
    @GetMapping("/charges")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    public ResponseEntity<ApiResponse<List<ChargeResponse>>> listCharges() {
        return ResponseEntity.ok(ApiResponse.<List<ChargeResponse>>builder()
                .success(true)
                .message("Charges retrieved")
                .data(tariffService.listCharges())
                .build());
    }

    @Operation(summary = "Deactivate charge")
    @PatchMapping("/charges/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChargeResponse>> deactivateCharge(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<ChargeResponse>builder()
                .success(true)
                .message("Charge deactivated")
                .data(tariffService.deactivateCharge(id))
                .build());
    }
}
