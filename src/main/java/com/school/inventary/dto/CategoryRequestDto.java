package com.school.inventary.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 150, message = "Category name cannot exceed 150 characters")
    private String categoryName;
}
