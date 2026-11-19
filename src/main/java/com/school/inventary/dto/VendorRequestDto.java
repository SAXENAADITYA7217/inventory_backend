package com.school.inventary.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorRequestDto {

    @NotBlank(message = "Vendor name is required")
    @Size(min = 2, max = 150, message = "Vendor name must be between 2 and 150 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Vendor name contains invalid characters")
    private String vendorName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must contain exactly 10 digits without alphabets or special characters"
    )
    private String phoneNo;

    @Pattern(
            regexp = "(?i)^$|^[0-9A-Z]{15}$",
            message = "GST number must contain 15 alphanumeric characters"
    )
    private String gstNo;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status = 1;
}
