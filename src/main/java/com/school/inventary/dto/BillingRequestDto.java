package com.school.inventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDto {

    @Size(max = 150, message = "Customer name cannot exceed 150 characters")
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\s.'#,-]+$", message = "Customer name contains invalid characters")
    private String customerName;

    @Size(max = 150, message = "Student name cannot exceed 150 characters")
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\s.'#,-]+$", message = "Student name contains invalid characters")
    private String studentName;

    @Size(max = 150, message = "Father name cannot exceed 150 characters")
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\s.'#,-]+$", message = "Father name contains invalid characters")
    private String fatherName;

    @Size(max = 50, message = "Class name cannot exceed 50 characters")
    @Pattern(regexp = "^$|^[a-zA-Z0-9\\s.'#,-]+$", message = "Class name contains invalid characters")
    private String className;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Mobile number must contain exactly 10 digits without alphabets or special characters")
    private String phoneNo;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String otherNote;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", message = "Amount paid cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Amount paid must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amountPaid;

    @NotEmpty(message = "At least one billing item is required")
    @Valid
    private List<BillingItemRequestDto> items;

    public void setStudentClass(String studentClass) {
        if (this.className == null || this.className.isBlank()) {
            this.className = studentClass;
        }
    }

    public String getStudentClass() {
        return this.className;
    }
}
