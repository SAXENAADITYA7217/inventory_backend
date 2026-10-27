package com.school.inventary.repo;

import com.school.inventary.entity.StockEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockEntryRepository extends JpaRepository<StockEntryEntity, Long> {

    Optional<StockEntryEntity> findByMasterIdAndStockId(
            String masterId, String stockId
    );

    List<StockEntryEntity> findByMasterIdOrderByCreatedAtDesc(String masterId);

    List<StockEntryEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(String masterId, Integer status);

    Boolean existsByMasterIdAndStockId(String masterId, String stockId);

}
