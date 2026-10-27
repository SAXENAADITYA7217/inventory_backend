package com.school.inventary.repo;

import com.school.inventary.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByCategoryId(String categoryId);

    boolean existsByCategoryId(String categoryId);

    List<CategoryEntity> findByStatus(Integer status);

    List<CategoryEntity> findByCategoryNameContainingIgnoreCase(String categoryName);

    List<CategoryEntity> findByMasterId(String masterId);

    boolean existsByMasterIdAndCategoryId(String masterId, String categoryId);

    boolean existsByMasterIdAndCategoryNameIgnoreCase(
            String masterId,
            String categoryName
    );


    Optional<CategoryEntity> findByCategoryIdAndMasterId(
            String categoryId,
            String masterId
    );



    // Sirf active categories, newest first
    List<CategoryEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(
            String masterId, Integer status
    );

    // Category ko usi school ke andar find karna
    Optional<CategoryEntity> findByIdAndMasterId(Long id, String masterId);

    boolean existsByMasterIdAndCategoryNameIgnoreCaseAndStatus(
            String masterId,
            String categoryName,
            Integer status
    );

    boolean existsByMasterIdAndCategoryNameIgnoreCaseAndCategoryIdNotAndStatus(
            String masterId,
            String categoryName,
            String categoryId,
            Integer status
    );

    boolean existsByMasterIdAndCategoryNameIgnoreCaseAndIdNotAndStatus(
            String masterId,
            String categoryName,
            Long id,
            Integer status
    );

    List<CategoryEntity> findByMasterIdAndCategoryNameIgnoreCase(
            String masterId,
            String categoryName
    );

    Optional<CategoryEntity> findByMasterIdAndCategoryNameIgnoreCaseAndStatus(
            String masterId,
            String categoryName,
            Integer status
    );


}