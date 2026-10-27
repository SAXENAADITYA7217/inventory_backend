package com.school.inventary.repo;

import com.school.inventary.entity.ProductEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByProductId(String productId);

    Optional<ProductEntity> findByProductIdAndMasterId(
            String productId,
            String masterId
    );

    boolean existsByMasterIdAndCategoryIdAndProductNameIgnoreCaseAndStatus(
            String masterId,
            String categoryId,
            String productName,
            Integer status
    );

    boolean existsByMasterIdAndCategoryIdAndProductNameIgnoreCaseAndProductIdNotAndStatus(
            String masterId,
            String categoryId,
            String productName,
            String productId,
            Integer status
    );

    List<ProductEntity>
    findByMasterIdAndCategoryIdAndStatusOrderByCreatedAtDesc(
            String masterId,
            String categoryId,
            Integer status
    );

    List<ProductEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(
            String masterId,
            Integer status
    );

    List<ProductEntity> findByMasterIdAndCategoryId(
            String masterId,
            String categoryId
    );
}