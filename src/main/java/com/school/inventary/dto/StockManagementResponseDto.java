
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class StockManagementResponseDto {

    private String productId;
    private String productName;
    private String uom;
    private String description;

    private BigDecimal openingStock;
    private BigDecimal currentStock;

    private LocalDate lastUpdateDate;
}
