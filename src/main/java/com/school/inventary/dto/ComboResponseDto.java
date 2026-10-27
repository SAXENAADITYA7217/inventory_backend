
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ComboResponseDto {

    private String comboId;
    private String comboName;
    private Integer status;
    private LocalDateTime createdAt;
    private List<ComboItemResponseDto> items;
}
