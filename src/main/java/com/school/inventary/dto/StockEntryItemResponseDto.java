
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class StockEntryItemResponseDto {

    private String productId;
    private String productName;
    private String uom;
    private String description;
    private BigDecimal rate;
    private BigDecimal quantity;
}
