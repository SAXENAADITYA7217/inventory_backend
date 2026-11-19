package com.school.inventary.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDto {

    @NotBlank(message = "Category ID is required")
    @Size(max = 50, message = "Category ID cannot exceed 50 characters")
    private String categoryId;

    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 150, message = "Product name cannot exceed 150 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Product name contains invalid characters")
    private String productName;

    @NotBlank(message = "UOM is required")
    @Size(max = 20, message = "UOM cannot exceed 20 characters")
    @Pattern(regexp = "^[^<>]+$", message = "UOM contains invalid characters")
    private String uom;

    @Size(max = 500, message = "Product description cannot exceed 500 characters")
    private String productDescription;

    @NotNull(message = "Minimum quantity is required")
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    @Max(value = 9999999, message = "Minimum quantity exceeds maximum limit")
    private Integer minQuantity;

    @Size(max = 100, message = "Rack detail cannot exceed 100 characters")
    private String rackDetail;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", message = "Selling price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Selling price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal sellingPrice;

    // Index of this product's image in the multipart "images" list.
    // Null means no new image was supplied.
    private Integer imageIndex;
}
