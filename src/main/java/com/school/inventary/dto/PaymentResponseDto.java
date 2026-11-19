package com.school.inventary.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private String paymentId;
    private String billingId;
    private BigDecimal paymentAmount;
    private String paymentMethod;
    private String transactionReference;
    private String note;
    private LocalDate paymentDate;
    private LocalTime paymentTime;

    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private String paymentStatus;

    private String customerName;
    private String studentName;
    private String fatherName;
    private String className;
    private String studentClass;
    private String phoneNo;
}