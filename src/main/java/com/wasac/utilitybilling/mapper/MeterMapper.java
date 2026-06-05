package com.wasac.utilitybilling.mapper;

import com.wasac.utilitybilling.domain.Meter;
import com.wasac.utilitybilling.dto.MeterResponse;
import org.springframework.stereotype.Component;

@Component
public class MeterMapper {
    public MeterResponse toResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .type(meter.getType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullName())
                .customerEmail(meter.getCustomer().getEmail())
                .createdAt(meter.getCreatedAt())
                .updatedAt(meter.getUpdatedAt())
                .build();
    }
}
