package com.school.inventary.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "stock_entry_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_entry_master_stock",
                        columnNames = {"master_id","stock_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_stock_entry_master_date",
                        columnList = "master_id,stock_date"
                ),
                @Index(
                        name = "idx,stock_entry_vendro",
                        columnList = "master_id, vendor_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StockEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "stock_id", nullable = false, length = 50)
    private String stockId;

    @Column(name = "vendor_id", nullable = false, length = 50)
    private String vendorId;

    @Column(name = "invoice_no", nullable = false, length = 100)
    private String invoiceNo;

    @Column(name = "other_note", nullable = false, length = 150)
    private String otherNote;

    @Column(name = "received_person", nullable = false, length = 150)
    private String receivedPerson;

    @Column(name = "invoice_dae")
    private LocalDate invoiceDate;

    @Column(name = "attach_document_link", length = 100000)
    private String attachDocumentLink;

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "stock_time")
    private LocalTime stockTime;

    @Column(name = "stock_date", nullable = false)
    private LocalDate stockDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        if(stockId==null || stockId.isBlank()){
            stockId = "STK_"+ UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase();
        }
        if(status==null){
            status = 1;
        }
        if(stockDate==null){
            stockDate = LocalDate.now();
        }
        if(stockTime==null){
            stockTime = LocalTime.now();
        }
        if(createdAt==null){
            createdAt = LocalDateTime.now();
        }
    }




}
