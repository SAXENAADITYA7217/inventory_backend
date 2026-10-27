
package com.school.inventary.service;

import com.school.inventary.dto.UserRequestDto;
import com.school.inventary.dto.UserResponseDto;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Register a SCHOOL user: role = 1
    @Transactional
    public UserResponseDto registerSchool(UserRequestDto request) {
        return createUser(request, 1);
    }

    // Register an ADMIN user: role = 0
    // Keep this endpoint protected.
    @Transactional
    public UserResponseDto registerAdmin(UserRequestDto request) {
        return createUser(request, 0);
    }

    private UserResponseDto createUser(
            UserRequestDto request,
            int role
    ) {
        String email = request.getEmail().trim();

        UserEntity existingUser = userRepository
                .findByEmail(email)
                .orElse(null);

        if (existingUser != null) {

            // An inactive school account already exists.
            // Do not create a duplicate or activate it automatically.
            if (role == 1
                    && existingUser.getRole() != null
                    && existingUser.getRole() == 1
                    && existingUser.getStatus() != null
                    && existingUser.getStatus() == 0) {

                throw new IllegalArgumentException(
                        "Your school account already exists and is awaiting "
                                + "admin approval. Please contact the admin."
                );
            }

            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        UserEntity user = new UserEntity();

        user.setMasterId(
                "USR-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .toUpperCase()
        );

        user.setName(request.getName());
        user.setSchoolName(request.getSchoolName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(email);

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Role is decided by the service.
        user.setRole(role);

        // All newly registered accounts require approval.
        user.setStatus(0);

        user.setAddress(request.getAddress());

        UserEntity savedUser = userRepository.save(user);

        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return mapToDto(user);
    }

    private UserResponseDto mapToDto(UserEntity user) {

        UserResponseDto response = new UserResponseDto();

        response.setId(user.getId());
        response.setMasterId(user.getMasterId());
        response.setName(user.getName());
        response.setSchoolName(user.getSchoolName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setAddress(user.getAddress());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }
}
