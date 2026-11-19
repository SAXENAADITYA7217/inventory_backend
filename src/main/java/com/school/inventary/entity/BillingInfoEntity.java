package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billing_info_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_billing_master_billing",
                        columnNames = {"master_id", "billing_id"}
                )
        },
        indexes = {
                @Index(name = "idx_billing_master_date",
                        columnList = "master_id,billing_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 100)
    private String masterId;

    @Column(name = "billing_id", nullable = false, length = 100)
    private String billingId;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "student_name", length = 150)
    private String studentName;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "phone_no", length = 15)
    private String phoneNo;

    @Column(name = "other_note", length = 500)
    private String otherNote;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_discount", precision = 12, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    @Column(name = "billing_time", nullable = false)
    private LocalTime billingTime;

    // PAID, PARTIAL, UNPAID
    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    // 1 = Active, 0 = Cancelled
    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (totalDiscount == null) totalDiscount = BigDecimal.ZERO;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (amountPaid == null) amountPaid = BigDecimal.ZERO;
        if (balance == null) balance = BigDecimal.ZERO;
        if (totalItems == null) totalItems = 0;
        if (status == null) status = 1;
        if (billingDate == null) billingDate = LocalDate.now();
        if (billingTime == null) billingTime = LocalTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}