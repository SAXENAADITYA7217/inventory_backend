
package com.school.inventary.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VendorResponseDto {

    private String vendorId;
    private String vendorName;
    private String phoneNo;
    private String gstNo;
    private String address;
    private Integer status;
    private LocalDateTime createdAt;
}
