
package com.school.inventary.security;

import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.UserRepository;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // No Bearer token: let Spring Security handle the request.
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        if (token.isEmpty()) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract email from JWT.
            String email = jwtService.extractEmail(token);

            if (email == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Do not overwrite existing authentication.
            if (SecurityContextHolder.getContext()
                    .getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Always check the current account in the database.
            UserEntity user = userRepository.findByEmail(email)
                    .orElse(null);

            // Reject missing or inactive accounts.
            if (user == null
                    || user.getStatus() == null
                    || user.getStatus() != 1) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Validate JWT signature and expiration.
            if (!jwtService.isTokenValid(token, email)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Compare JWT tokenVersion with the database.
            Claims claims = jwtService.extractAllClaims(token);

            Number jwtVersion =
                    claims.get("tokenVersion", Number.class);

            long currentVersion = user.getTokenVersion() == null
                    ? 0L
                    : user.getTokenVersion();

            if (jwtVersion == null
                    || jwtVersion.longValue() != currentVersion) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Only two roles exist in this application.
            String authority;

            if (user.getRole() != null && user.getRole() == 0) {
                authority = "ROLE_ADMIN";
            } else if (user.getRole() != null
                    && user.getRole() == 1) {
                authority = "ROLE_SCHOOL";
            } else {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // Authenticate the user with exactly one authority.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(new SimpleGrantedAuthority(authority))
                    );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (Exception exception) {
            // Invalid, malformed, or expired JWT.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
