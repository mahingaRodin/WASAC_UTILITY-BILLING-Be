package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.BillLineItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BillLineItemRequest {
    @NotNull
    private BillLineItemType itemType;
    @NotBlank
    private String description;
    @NotNull
    private BigDecimal quantity;
    @NotNull
    private BigDecimal unitPrice;
}
