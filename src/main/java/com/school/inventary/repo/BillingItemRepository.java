package com.school.inventary.repo;

import com.school.inventary.entity.BillingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingItemRepository
        extends JpaRepository<BillingItemEntity, Long> {

    List<BillingItemEntity> findByMasterIdAndBillingIdOrderByIdAsc(
            String masterId, String billingId);

    List<BillingItemEntity> findByMasterIdAndProductId(
            String masterId, String productId);

    boolean existsByMasterIdAndBillingIdAndProductId(
            String masterId, String billingId, String productId);
}