package com.school.inventary.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String masterId;

    private String name;
    private String schoolName;
    private String phoneNumber;
    private String email;

    private Integer role;
    private Integer status;

    private String address;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}