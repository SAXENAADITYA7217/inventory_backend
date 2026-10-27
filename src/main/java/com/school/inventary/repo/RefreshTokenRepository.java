
package com.school.inventary.repo;

import com.school.inventary.entity.RefreshTokenEntity;
import com.school.inventary.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findByUserId(Long userId);

    List<RefreshTokenEntity> findAllByUserAndRevokedFalse(
            UserEntity user
    );
}
