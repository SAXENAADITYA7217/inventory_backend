
package com.school.inventary.service;

import com.school.inventary.dto.UserResponseDto;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    // Get all users, newest first
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers(String loggedInEmail) {

        getActiveAdmin(loggedInEmail);

        return userRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // Activate or deactivate a school account
    @Transactional
    public UserResponseDto updateUserStatus(
            String masterId,
            Integer status,
            String loggedInEmail) {

        // Verify logged-in admin
        getActiveAdmin(loggedInEmail);

        // Validate requested status
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException(
                    "Status must be 1 (ACTIVE) or 0 (INACTIVE)"
            );
        }

        // Find school account using masterId
        UserEntity user = userRepository.findByMasterId(masterId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        // Only school accounts can be managed
        if (user.getRole() == null || user.getRole() != 1) {
            throw new IllegalArgumentException(
                    "Only school accounts can be activated or deactivated"
            );
        }

        // Prevent duplicate status updates
        if (user.getStatus() != null
                && user.getStatus().equals(status)) {

            throw new IllegalArgumentException(
                    status == 1
                            ? "User is already active"
                            : "User is already inactive"
            );
        }

        // Update status
        user.setStatus(status);

        // Invalidate tokens/sessions when account status changes
        Long currentVersion = user.getTokenVersion();
        user.setTokenVersion(
                (currentVersion == null ? 0L : currentVersion) + 1L
        );

        UserEntity savedUser = userRepository.save(user);

        // Revoke all refresh tokens belonging to this user
        refreshTokenService.revokeAllUserTokens(savedUser);

        return mapToDto(savedUser);
    }

    // Verify that the logged-in account is an active admin
    private UserEntity getActiveAdmin(String email) {

        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin not found"));

        if (admin.getRole() == null || admin.getRole() != 0) {
            throw new IllegalArgumentException(
                    "Only admin can perform this operation"
            );
        }

        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "Admin account is inactive"
            );
        }

        return admin;
    }

    // Entity to response DTO
    private UserResponseDto mapToDto(UserEntity user) {

        return UserResponseDto.builder()
                .id(user.getId())
                .masterId(user.getMasterId())
                .name(user.getName())
                .schoolName(user.getSchoolName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
