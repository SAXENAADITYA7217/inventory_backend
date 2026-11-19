package com.school.inventary.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingResponseDto {

    private String billingId;
    private String customerName;
    private String studentName;
    private String fatherName;
    private String className;
    private String studentClass;
    private String phoneNo;
    private String otherNote;

    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;

    private Integer totalItems;
    private String paymentStatus;
    private Integer status;

    private LocalDate billingDate;
    private LocalTime billingTime;

    private List<BillingItemResponseDto> items;
}