
package com.school.inventary.repo;

import com.school.inventary.entity.ComboEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComboRepository
        extends JpaRepository<ComboEntity, Long> {

    Optional<ComboEntity> findByComboIdAndMasterId(
            String comboId,
            String masterId
    );

    List<ComboEntity> findByMasterIdAndStatusOrderByCreatedAtDesc(
            String masterId,
            Integer status
    );

    boolean existsByMasterIdAndComboNameIgnoreCaseAndStatus(
            String masterId,
            String comboName,
            Integer status
    );
}
