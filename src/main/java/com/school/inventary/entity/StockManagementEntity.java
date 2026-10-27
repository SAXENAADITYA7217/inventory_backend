package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "stock_management_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_master_product",
                        columnNames = {"master_id", "product_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockManagementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal openingStock = BigDecimal.ZERO;

    private LocalDate lastUpdateDate;

    @PrePersist
    public void prePersist(){
        if(quantity==null){
            quantity = BigDecimal.ZERO;
        }
        if(openingStock==null){
            openingStock = BigDecimal.ZERO;
        }
        if(lastUpdateDate==null){
            lastUpdateDate = LocalDate.now();
        }
    }

    @PreUpdate
    public void preUpdate(){
        lastUpdateDate = LocalDate.now();
    }

}
