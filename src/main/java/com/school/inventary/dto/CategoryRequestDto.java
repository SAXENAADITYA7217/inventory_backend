package com.school.inventary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 150, message = "Category name cannot exceed 150 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Category name contains invalid characters")
    private String categoryName;
}
