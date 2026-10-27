
package com.school.inventary.security;

import com.school.inventary.entity.UserEntity;
import com.school.inventary.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UserEntity existingUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // this code edited by antigravity
        java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        int role = existingUser.getRole() != null ? existingUser.getRole() : -1;
        switch (role) {
            case 0 -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
            case 1 -> {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SCHOOL"));
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            case 2 -> {
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SCHOOL"));
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STOREKEEPER"));
            }
            case 3 -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TEACHER"));
            default -> authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
        }

        return User.builder()
                .username(existingUser.getEmail())
                .password(existingUser.getPassword())
                .authorities(authorities)
                .build();
        // this code edited by antigravity
    }
}
