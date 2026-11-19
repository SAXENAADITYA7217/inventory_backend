package com.school.inventary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StockEntryRequestDto {

    @NotBlank(message = "Vendor ID is required")
    @Size(max = 50, message = "Vendor ID cannot exceed 50 characters")
    private String vendorId;

    @NotBlank(message = "Invoice number is required")
    @Size(min = 1, max = 100, message = "Invoice number cannot exceed 100 characters")
    @Pattern(regexp = "^[^<>]+$", message = "Invoice number contains invalid characters")
    private String invoiceNo;

    @NotNull(message = "Invoice date is required")
    @PastOrPresent(message = "Invoice date cannot be in the future")
    private LocalDate invoiceDate;

    @NotBlank(message = "Received person is required")
    @Size(min = 2, max = 100, message = "Received person name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Received person must contain only letters, spaces, hyphens, and dots")
    private String receivedPerson;

    @Size(max = 150, message = "Notes cannot exceed 150 characters")
    private String otherNote;

    private MultipartFile attachDocument;

    @NotEmpty(message = "At least one product item is required")
    @Valid
    private List<StockEntryItemRequestDto> items;
}
