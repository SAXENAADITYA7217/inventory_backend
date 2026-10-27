package com.school.inventary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {

    @NotBlank(message = "Category ID is required")
    @Size(max = 50)
    private String categoryId;

    @NotBlank(message = "Product name is required")
    @Size(max = 150)
    private String productName;

    @NotBlank(message = "UOM is required")
    @Size(max = 20)
    private String uom;

    @Size(max = 500)
    private String productDescription;

    @NotNull(message = "Minimum quantity is required")
    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minQuantity;

    @Size(max = 100)
    private String rackDetail;

    // Index of this product's image in the multipart "images" list.
    // Null means no new image was supplied.
    private Integer imageIndex;
}