package com.school.inventary.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingItemResponseDto {

    private String itemId;
    private String productId;
    private String productName;
    private String uom;
    private BigDecimal quantity;
    private BigDecimal sellingPrice;
    private BigDecimal discount;
    private BigDecimal totalAmount;
}