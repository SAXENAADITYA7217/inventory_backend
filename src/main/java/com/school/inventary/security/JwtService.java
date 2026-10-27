
package com.school.inventary.security;

import com.school.inventary.entity.UserEntity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long expiration) {

        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );

        if (expiration <= 0) {
            throw new IllegalArgumentException(
                    "JWT access-token expiration must be positive"
            );
        }

        this.accessTokenExpiration = expiration;
    }

    public String generateAccessToken(UserEntity user) {

        Date now = new Date();
        Date expiry = new Date(
                now.getTime() + accessTokenExpiration
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .claim(
                        "tokenVersion",
                        user.getTokenVersion() == null
                                ? 0L
                                : user.getTokenVersion()
                )
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        try {
            Claims claims = extractAllClaims(token);

            return claims.getSubject() != null
                    && claims.getSubject().equals(email)
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }
}
