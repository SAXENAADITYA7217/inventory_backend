package com.school.inventary.repo;

import com.school.inventary.entity.StockManagementEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockManagementRepository extends JpaRepository<StockManagementEntity, Long> {
    Optional<StockManagementEntity> findByMasterIdAndProductId(String masterId, String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockManagementEntity s WHERE s.masterId = :masterId AND s.productId = :productId")
    Optional<StockManagementEntity> findByMasterIdAndProductIdForUpdate(
            @Param("masterId") String masterId,
            @Param("productId") String productId
    );

    List<StockManagementEntity> findByMasterId(String masterId);

    boolean existsByMasterIdAndProductId(String masterId, String productId);
}
