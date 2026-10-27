package com.school.inventary.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_entry_data_tbl",
        indexes = {
                @Index(
                        name = "idx_stock_entry_data_master_stock",
                        columnList = "master_id, stock_id"
                ),
                @Index(
                        name = "idx_stock_entry_data_product",
                        columnList = "master_id, product_id"
                )
        }
)

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockEntryDataEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "stock_id", nullable = false, length = 50)
    private String stockId;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Builder.Default
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        if(status==null){
            status = 1;
        }
        if(createdAt==null){
            createdAt = LocalDateTime.now();
        }
    }


}
