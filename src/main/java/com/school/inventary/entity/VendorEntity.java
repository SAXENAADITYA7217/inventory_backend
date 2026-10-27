
package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "erp_vendor_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vendor_master_vendor",
                        columnNames = {"master_id", "vendor_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_vendor_master_status",
                        columnList = "master_id, status"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VendorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false, length = 50)
    private String masterId;

    @Column(name = "vendor_id", nullable = false, length = 50)
    private String vendorId;

    @Column(name = "vendor_name", nullable = false, length = 150)
    private String vendorName;

    @Column(name = "phone_no", nullable = false, length = 15)
    private String phoneNo;

    @Column(name = "gst_no", length = 15)
    private String gstNo;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (vendorId == null || vendorId.isBlank()) {
            vendorId = "VND-" + UUID.randomUUID()
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
