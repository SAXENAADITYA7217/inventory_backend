package com.school.inventary.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Payment amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal paymentAmount;

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(CASH|UPI|CARD|BANK|OTHER)$", message = "Payment method must be CASH, UPI, CARD, BANK, or OTHER")
    private String paymentMethod;

    @Size(max = 100, message = "Transaction reference cannot exceed 100 characters")
    private String transactionReference;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;
}
