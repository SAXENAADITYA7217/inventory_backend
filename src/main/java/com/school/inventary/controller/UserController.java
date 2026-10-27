
package com.school.inventary.controller;

import com.school.inventary.dto.UserRequestDto;
import com.school.inventary.dto.UserResponseDto;
import com.school.inventary.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto registerSchool(
            @RequestBody UserRequestDto request) {
        return userService.registerSchool(request);
    }

    @GetMapping("/me")
    public UserResponseDto getMyProfile(
            Authentication authentication) {

        // this code edited by antigravity
        if (authentication == null || authentication.getName() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User is not authenticated"
            );
        }
        // this code edited by antigravity

        return userService.getUserByEmail(authentication.getName());
    }

}
