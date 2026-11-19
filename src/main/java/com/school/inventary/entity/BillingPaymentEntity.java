package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billing_payment_tbl",
        indexes = {
                @Index(
                        name = "idx_payment_master_bill",
                        columnList = "master_id,billing_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 100)
    private String masterId;

    @Column(name = "billing_id", nullable = false, length = 100)
    private String billingId;

    @Column(name = "payment_id", nullable = false, unique = true, length = 100)
    private String paymentId;

    @Column(name = "payment_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentAmount;

    // CASH, UPI, CARD, BANK
    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "transaction_reference", length = 150)
    private String transactionReference;

    @Column(name = "note", length = 300)
    private String note;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_time", nullable = false)
    private LocalTime paymentTime;

    // 1 = Active, 0 = Reversed
    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) status = 1;
        if (paymentDate == null) paymentDate = LocalDate.now();
        if (paymentTime == null) paymentTime = LocalTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}