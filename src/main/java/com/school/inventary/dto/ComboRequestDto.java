
package com.school.inventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComboRequestDto {

    @NotBlank(message = "Combo name is required")
    @Size(max = 150, message = "Combo name cannot exceed 150 characters")
    private String comboName;

    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<ComboItemRequestDto> items;
}
