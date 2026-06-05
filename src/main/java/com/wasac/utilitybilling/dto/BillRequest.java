package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.BillStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BillRequest {
    @NotBlank
    private String billReference;
    @NotNull
    private UUID customerId;
    @NotNull
    private UUID meterId;
    @NotNull
    private Integer billingYear;
    @NotNull
    @Min(1)
    @Max(12)
    private Integer billingMonth;
    @NotNull
    private BigDecimal unitsConsumed;
    @NotNull
    private BigDecimal amountDue;
    @NotNull
    private LocalDate dueDate;
    @NotNull
    private BillStatus status;
    @Valid
    private List<BillLineItemRequest> lineItems;
}
