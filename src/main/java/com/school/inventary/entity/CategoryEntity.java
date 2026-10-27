package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "erp_category_tbl"
)
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "category_id", nullable = false, length = 50)
    private String categoryId;

    @Column(name = "category_name", nullable = false, length = 150)
    private String categoryName;

    // 1 = ACTIVE, 0 = INACTIVE
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = 1;
        }
    }
}