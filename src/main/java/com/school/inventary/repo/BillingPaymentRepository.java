package com.school.inventary.repo;

import com.school.inventary.entity.BillingPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingPaymentRepository
        extends JpaRepository<BillingPaymentEntity, Long> {

    List<BillingPaymentEntity>
    findByMasterIdAndBillingIdAndStatusOrderByCreatedAtAsc(
            String masterId,
            String billingId,
            Integer status
    );

    boolean existsByPaymentId(String paymentId);
}