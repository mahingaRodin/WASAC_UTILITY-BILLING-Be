package com.wasac.utilitybilling.controller;

import com.wasac.utilitybilling.domain.ChargeConfiguration;
import com.wasac.utilitybilling.domain.TariffConfiguration;
import com.wasac.utilitybilling.dto.ApiResponse;
import com.wasac.utilitybilling.dto.ChargeConfigurationRequest;
import com.wasac.utilitybilling.dto.TariffConfigurationRequest;
import com.wasac.utilitybilling.service.TariffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class TariffController {
    private final TariffService tariffService;

    @PostMapping("/tariffs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TariffConfiguration>> createTariff(@Valid @RequestBody TariffConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponse.<TariffConfiguration>builder()
                .success(true)
                .message("Tariff configuration created")
                .data(tariffService.createTariff(request))
                .build());
    }

    @PostMapping("/charges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChargeConfiguration>> createCharge(@Valid @RequestBody ChargeConfigurationRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChargeConfiguration>builder()
                .success(true)
                .message("Charge configuration created")
                .data(tariffService.createCharge(request))
                .build());
    }
}
