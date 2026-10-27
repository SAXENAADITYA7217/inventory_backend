package com.school.inventary.repo;

import com.school.inventary.entity.StockEntryDataEntity;
import com.school.inventary.entity.StockEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockEntryDataRepository extends JpaRepository<StockEntryDataEntity, Long> {

    List<StockEntryDataEntity> findByMasterIdAndStockIdOrderByIdAsc(String masterId, String stockId );

    List<StockEntryDataEntity> findByMasterIdAndProductIdOrderByCreatedAtDesc(String masterId, String productId);

    boolean existsByMasterIdAndStockIdAndProductId(
            String masterId,
            String stockId,
            String productId
    );

}
