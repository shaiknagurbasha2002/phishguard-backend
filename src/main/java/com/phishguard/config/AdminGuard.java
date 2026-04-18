package com.phishguard.config;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Checks if the currently authenticated user has ROLE_ADMIN.
 * Reads from Spring Security context (JWT already validated by JwtFilter).
 * No need for X-User-Id header — role comes from the JWT token.
 */
@Component
public class AdminGuard {

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    public <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(403).build();
    }
}
