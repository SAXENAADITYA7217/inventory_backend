
package com.school.inventary.service;

import com.school.inventary.dto.AuthResponseDto;
import com.school.inventary.dto.LoginRequestDto;
import com.school.inventary.dto.RefreshTokenRequestDto;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.UserRepository;
import com.school.inventary.security.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // Login
    public AuthResponseDto login(LoginRequestDto request) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Check whether account is active
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is inactive"
            );
        }

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Generate access token
        String accessToken = jwtService.generateAccessToken(user);

        // Generate refresh token
        RefreshTokenService.IssuedToken issued =
                refreshTokenService.issueToken(user);

        // Return both tokens and role
        return new AuthResponseDto(
                accessToken,
                issued.token(),
                accessTokenExpiration / 1000,
                user.getRole()
        );
    }

    // Refresh access token and rotate refresh token
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {

        try {
            RefreshTokenService.IssuedToken issued =
                    refreshTokenService.rotateToken(
                            request.getRefreshToken()
                    );

            UserEntity user = issued.user();

            // Generate new access token
            String accessToken =
                    jwtService.generateAccessToken(user);

            // Return new tokens and user's role
            return new AuthResponseDto(
                    accessToken,
                    issued.token(),
                    accessTokenExpiration / 1000,
                    user.getRole()
            );

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid, expired or revoked refresh token"
            );
        }
    }

    // Logout and revoke all sessions
    @Transactional
    public void logout(RefreshTokenRequestDto request) {

        try {
            refreshTokenService.revokeAllSessions(
                    request.getRefreshToken()
            );

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or revoked refresh token"
            );
        }
    }
}
