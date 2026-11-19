package com.school.inventary.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingItemRequestDto {

    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID cannot exceed 50 characters")
    private String productId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.001",
            inclusive = true,
            message = "Quantity must be greater than zero"
    )
    @Digits(integer = 9, fraction = 3, message = "Quantity must have at most 9 integer digits and 3 decimal places")
    private BigDecimal quantity;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Discount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal discount;

    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Selling price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal sellingPrice;

    public void setFinalSellingPrice(BigDecimal finalSellingPrice) {
        if (this.sellingPrice == null && finalSellingPrice != null) {
            this.sellingPrice = finalSellingPrice;
        }
    }
}
