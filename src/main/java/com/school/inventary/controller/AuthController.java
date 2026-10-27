
package com.school.inventary.controller;

import com.school.inventary.dto.AuthResponseDto;
import com.school.inventary.dto.LoginRequestDto;
import com.school.inventary.dto.RefreshTokenRequestDto;
import com.school.inventary.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody LoginRequestDto request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @RequestBody RefreshTokenRequestDto request) {

        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody RefreshTokenRequestDto request) {

        authService.logout(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Logged out successfully");
    }
}
