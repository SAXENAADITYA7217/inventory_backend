package com.school.inventary.dto;

import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID cannot exceed 50 characters")
    private String productId;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Rate must have at most 10 integer digits and 2 decimal places")
    private BigDecimal rate;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.001",
            inclusive = true,
            message = "Quantity must be greater than zero"
    )
    @Digits(integer = 9, fraction = 3, message = "Quantity must have at most 9 integer digits and 3 decimal places")
    private BigDecimal quantity;
}
