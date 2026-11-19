package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billing_item_tbl",
        indexes = {
                @Index(name = "idx_billing_item_master_bill",
                        columnList = "master_id,billing_id"),
                @Index(name = "idx_billing_item_product",
                        columnList = "master_id,product_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 100)
    private String masterId;

    @Column(name = "billing_id", nullable = false, length = 100)
    private String billingId;

    @Column(name = "item_id", nullable = false, length = 100)
    private String itemId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    // Product name snapshot
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    // UOM snapshot
    @Column(name = "uom", nullable = false, length = 50)
    private String uom;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    // Selling price snapshot
    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    // Discount amount for this product line
    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // 1 = Active, 0 = Cancelled
    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (discount == null) discount = BigDecimal.ZERO;
        if (status == null) status = 1;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}