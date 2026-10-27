
package com.school.inventary.repo;

import com.school.inventary.entity.VendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorRepository
        extends JpaRepository<VendorEntity, Long> {

    List<VendorEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(
            String masterId,
            Integer status
    );

    boolean existsByMasterIdAndGstNoIgnoreCaseAndStatus(
            String masterId,
            String gstNo,
            Integer status
    );

    Optional<VendorEntity> findByMasterIdAndVendorId(
            String masterId,
            String vendorId
    );


    boolean existsByMasterIdAndGstNoIgnoreCaseAndStatusAndVendorIdNot(
            String masterId,
            String gstNo,
            Integer status,
            String vendorId
    );

    Optional<VendorEntity> findByMasterIdAndVendorIdAndStatus(
            String masterId,
            String vendorId,
            Integer status
    );

    List<VendorEntity> findByMasterIdOrderByCreatedAtDesc(String masterId);

}
