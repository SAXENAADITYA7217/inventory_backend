package com.school.inventary.repo;

import com.school.inventary.entity.StockManagementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockManagementRepository extends JpaRepository<StockManagementEntity, Long> {
    Optional<StockManagementEntity> findByMasterIdAndProductId(String masterId, String productId);

    List<StockManagementEntity> findByMasterId(String masterId);

    boolean existsByMasterIdAndProductId(String masterId, String productId);



}

