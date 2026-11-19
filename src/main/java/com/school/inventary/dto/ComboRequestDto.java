package com.school.inventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComboRequestDto {

    @NotBlank(message = "Combo name is required")
    @Size(min = 1, max = 150, message = "Combo name cannot exceed 150 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Combo name contains invalid characters")
    private String comboName;

    @NotEmpty(message = "At least one product item is required")
    @Valid
    private List<ComboItemRequestDto> items;
}
