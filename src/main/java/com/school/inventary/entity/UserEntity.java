
package com.school.inventary.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users_tbl")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", unique = true, nullable = false)
    private String masterId;

    @Column(nullable = false)
    private String name;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // 1 = ADMIN, 2 = STOREKEEPER, 3 = TEACHER
    @Column(nullable = false)
    private Integer role;

    // 1 = ACTIVE, 0 = INACTIVE
    @Column(nullable = false)
    private Integer status;

    private String address;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion = 0L;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (tokenVersion == null) {
            tokenVersion = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
