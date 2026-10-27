
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class StockEntryResponseDto {

    private String stockId;

    private String vendorId;
    private String vendorName;

    private String invoiceNo;
    private LocalDate invoiceDate;

    private String receivedPerson;
    private String otherNote;

    private String attachDocumentLink;

    private Integer status;

    private LocalDate stockDate;
    private LocalDateTime createdAt;

    private List<StockEntryItemResponseDto> items;
}
