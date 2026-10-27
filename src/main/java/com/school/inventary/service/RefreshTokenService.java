
package com.school.inventary.service;

import com.school.inventary.entity.RefreshTokenEntity;
import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.RefreshTokenRepository;
import com.school.inventary.repo.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long expirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public record IssuedToken(UserEntity user, String token) {}

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash refresh token", exception
            );
        }
    }

    private IssuedToken createToken(UserEntity user) {
        String rawToken = generateRawToken();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(hashToken(rawToken));
        entity.setExpiresAt(
                LocalDateTime.now().plusNanos(expirationMs * 1_000_000L)
        );
        entity.setRevoked(false);

        repository.save(entity);

        return new IssuedToken(user, rawToken);
    }

    @Transactional
    public IssuedToken issueToken(UserEntity user) {
        return createToken(user);
    }

    @Transactional
    public IssuedToken rotateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token is required"
            );
        }

        RefreshTokenEntity oldToken = repository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );

        if (oldToken.isRevoked()
                || !oldToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Refresh token expired or revoked"
            );
        }

        UserEntity user = oldToken.getUser();

        if (user.getStatus() == null || user.getStatus() != 1) {
            oldToken.setRevoked(true);
            repository.save(oldToken);

            throw new IllegalArgumentException(
                    "User account is inactive"
            );
        }

        oldToken.setRevoked(true);
        repository.save(oldToken);

        return createToken(user);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        repository.findByTokenHash(hashToken(rawToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    repository.save(token);
                });
    }

    @Transactional
    public void revokeAllSessions(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Refresh token is required"
            );
        }

        RefreshTokenEntity refreshToken = repository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid refresh token"
                        )
                );

        UserEntity user = refreshToken.getUser();

        // Increment version to invalidate previously issued access JWTs.
        long currentVersion = user.getTokenVersion() == null
                ? 0L
                : user.getTokenVersion();

        user.setTokenVersion(currentVersion + 1L);
        userRepository.save(user);

        // Revoke every refresh token belonging to this user.
        List<RefreshTokenEntity> tokens =
                repository.findByUserId(user.getId());

        for (RefreshTokenEntity token : tokens) {
            token.setRevoked(true);
        }

        repository.saveAll(tokens);
    }
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {

        List<RefreshTokenEntity> tokens =
                repository.findByUserId(user.getId());

        for (RefreshTokenEntity token : tokens) {
            token.setRevoked(true);
        }

        repository.saveAll(tokens);
    }
}
