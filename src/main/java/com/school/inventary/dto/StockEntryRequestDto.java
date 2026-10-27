package com.school.inventary.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class StockEntryRequestDto {

    @NotBlank(message = "Vendor id is required")
    private String vendorId;

    @NotBlank(message = "invoice number is required")
    @Size(max = 100)
    private String invoiceNo;

    @NotNull(message = "invoice date is required")
    private LocalDate invoiceDate;

    @NotBlank(message = "Received person is required")
    @Size(max = 1000)
    private String receivedPerson;


    private String otherNote;

    private MultipartFile attachDocument;

    @NotEmpty(message = "At least one product is required")
    @Valid
    private List<StockEntryItemRequestDto> items;
}
