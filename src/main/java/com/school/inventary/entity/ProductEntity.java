package com.school.inventary.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "erp_product_tbl")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "category_id", nullable = false, length = 50)
    private String categoryId;


    @Column(name = "product_id", nullable = false, unique = true, length = 50)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    // Unit of Measurement: PCS, KG, GM, M, MM, IN
    @Column(name = "uom", nullable = false, length = 20)
    private String uom;

    @Column(name = "product_image_link", length = 500)
    private String productImageLink;

    @Column(name = "product_description" , length = 500)
    private String productDescription;


    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity =  0;

    @Column(name = "rack_detail", length = 100)
    private String rackDetail;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate(){
        if(productId==null || productId.isBlank()){
            productId = "PRD_"+ UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }

        if(minQuantity==null){
            minQuantity = 0;
        }
        if (status==null){
            status = 1;
        }
        if(createdAt==null){
            createdAt  = LocalDateTime.now();
        }
    }

//    create table erp_product_tbl (
//            id bigint not null auto_increment,
//            category_id varchar(50) not null,
//    created_at datetime(6) not null,
//    master_id varchar(50) not null,
//    min_quantity integer not null,
//    product_description varchar(500),
//    product_id varchar(50) not null,
//    product_image_link varchar(500),
//    product_name varchar(150) not null,
//    rack_detail varchar(100),
//    status integer not null,
//    uom varchar(20) not null,
//    primary key (id)
//    )

}
