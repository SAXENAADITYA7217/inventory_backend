
package com.school.inventary.controller;

import com.school.inventary.dto.UserRequestDto;
import com.school.inventary.dto.UserResponseDto;
import com.school.inventary.service.AdminService;
import com.school.inventary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;

    // Register admin
    @PostMapping("/register")
    public UserResponseDto registerAdmin(
            @RequestBody UserRequestDto request) {

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
            @PathVariable String masterId,
            @RequestParam Integer status,
            Authentication authentication) {

        return adminService.updateUserStatus(
                masterId,
                status,
                authentication.getName()
        );
    }
}
