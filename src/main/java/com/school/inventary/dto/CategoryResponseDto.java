package com.school.inventary.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder

public class CategoryResponseDto {

    private Long id;
    private String masterId;
    private String categoryId;
    private String categoryName;
    private Integer status;
    private LocalDateTime createdAt;

}
