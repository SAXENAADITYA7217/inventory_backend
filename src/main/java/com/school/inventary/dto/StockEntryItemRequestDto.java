package com.school.inventary.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockEntryItemRequestDto {

    @NotBlank(message = "Product Id is required")
    private String productId;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    private BigDecimal rate;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Quantity must be greater than zero"
    )
    private BigDecimal quantity;

}
