
package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "erp_product_combo_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_combo_master_combo",
                        columnNames = {"master_id", "combo_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_combo_master_status",
                        columnList = "master_id, status"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComboEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "combo_id", nullable = false, length = 50)
    private String comboId;

    @Column(name = "combo_name", nullable = false, length = 150)
    private String comboName;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (comboId == null || comboId.isBlank()) {
            comboId = "COMBO-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .toUpperCase();
        }

        if (status == null) {
            status = 1;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
