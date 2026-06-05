package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.MeterStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMeterStatusRequest {
    @NotNull
    private MeterStatus status;
}
