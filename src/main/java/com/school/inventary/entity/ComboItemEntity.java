
package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "erp_product_combo_item_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_combo_item_product",
                        columnNames = {
                                "master_id",
                                "combo_id",
                                "product_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_combo_item_master_combo",
                        columnList = "master_id, combo_id"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComboItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "combo_id", nullable = false, length = 50)
    private String comboId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
