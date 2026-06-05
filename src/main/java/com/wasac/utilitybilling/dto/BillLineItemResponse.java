package com.wasac.utilitybilling.dto;

import com.wasac.utilitybilling.domain.enums.BillLineItemType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class BillLineItemResponse {
    private UUID id;
    private BillLineItemType itemType;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
