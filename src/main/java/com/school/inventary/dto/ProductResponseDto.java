
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductResponseDto {

    private Long id;

    private String masterId;

    private String categoryId;

    private String productId;

    private String productName;

    private String uom;

    private String productImageLink;
    private String productImageUrl;

    private String productDescription;

    private Integer minQuantity;

    private String rackDetail;

    // 1 = ACTIVE, 0 = INACTIVE
    private Integer status;

    private LocalDateTime createdAt;
}
