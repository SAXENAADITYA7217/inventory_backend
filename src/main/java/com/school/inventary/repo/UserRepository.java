
package com.school.inventary.repo;

import com.school.inventary.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);


    Optional<UserEntity> findByMasterId(String masterId);


    boolean existsByEmail(String email);

    boolean existsByMasterId(String masterId);

    // Find users by status: 1 = Active, 0 = Inactive
    List<UserEntity> findByStatus(Integer status);

    // Find users by role: 1 = Admin, 2 = School
    List<UserEntity> findByRole(Integer role);

    // Find users by role and status
    List<UserEntity> findByRoleAndStatus(
            Integer role, Integer status);

    // Find user by email and status
    Optional<UserEntity> findByEmailAndStatus(
            String email, Integer status);
}
