package com.school.inventary.controller;

import com.school.inventary.dto.UserRequestDto;
import com.school.inventary.dto.UserResponseDto;
import com.school.inventary.service.AdminService;
import com.school.inventary.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    // Register admin
    @PostMapping("/register")
    public UserResponseDto registerAdmin(
            @Valid @RequestBody UserRequestDto request) {

        return userService.registerAdmin(request);
    }

    // Get all users
    @GetMapping("/users")
    public List<UserResponseDto> getAllUsers(
            Authentication authentication) {

        return adminService.getAllUsers(authentication.getName());
    }

    // Activate or deactivate a school account
    @PatchMapping("/users/{masterId}/status")
    public UserResponseDto updateUserStatus(
            @PathVariable @NotBlank(message = "Master ID is required") String masterId,
            @RequestParam @NotNull(message = "Status is required") @Min(value = 0, message = "Status must be 0 or 1") @Max(value = 1, message = "Status must be 0 or 1") Integer status,
            Authentication authentication) {

        return adminService.updateUserStatus(
                masterId,
                status,
                authentication.getName()
        );
    }
}
