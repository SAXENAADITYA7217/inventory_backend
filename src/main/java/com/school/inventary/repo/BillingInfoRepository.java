package com.school.inventary.repo;

import com.school.inventary.entity.BillingInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillingInfoRepository
        extends JpaRepository<BillingInfoEntity, Long> {

    Optional<BillingInfoEntity> findByMasterIdAndBillingId(
            String masterId, String billingId);

    List<BillingInfoEntity> findByMasterIdOrderByCreatedAtDesc(
            String masterId);

    List<BillingInfoEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(
            String masterId, Integer status);

    boolean existsByMasterIdAndBillingId(
            String masterId, String billingId);
}