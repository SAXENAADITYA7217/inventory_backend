package com.school.inventary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorRequestDto {

    @NotBlank(message = "Vendor name is required")
    @Size(max = 150)
    private String vendorName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian mobile number"
    )
    private String phoneNo;

    @Pattern(
            regexp = "(?i)^$|^[0-9A-Z]{15}$",
            message = "GST number must contain 15 alphanumeric characters"
    )
    private String gstNo;

    @Size(max = 500)
    private String address;

    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status = 1;
}