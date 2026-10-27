
package com.school.inventary.repo;

import com.school.inventary.entity.ComboItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboItemRepository
        extends JpaRepository<ComboItemEntity, Long> {

    List<ComboItemEntity> findByMasterIdAndComboIdOrderByIdAsc(
            String masterId,
            String comboId
    );

    boolean existsByMasterIdAndComboIdAndProductId(
            String masterId,
            String comboId,
            String productId
    );

    void deleteByMasterIdAndComboId(
            String masterId,
            String comboId
    );
}
