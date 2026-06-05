package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank
    private String billReference;
    @NotNull
    private BigDecimal amountPaid;
    @NotNull
    private PaymentMethod paymentMethod;
    @NotNull
    private LocalDate paymentDate;
}
