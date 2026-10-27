
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ComboItemResponseDto {

    private String productId;
    private String productName;
    private String categoryId;
    private String uom;
    private String productImageLink;
    private Integer quantity;
}
