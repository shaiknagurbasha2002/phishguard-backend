package com.phishguard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.phishguard.repository.UserRepository;

/**
 * Lightweight admin-role checker.
 *
 * Usage in a controller:
 *   if (!adminGuard.isAdmin(userIdHeader)) return adminGuard.forbidden();
 *
 * Frontend must send header:  X-User-Id: <userId>  on every admin request.
 */
@Component
public class AdminGuard {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns true only if the userId in the header belongs to a user whose role is "ADMIN".
     */
    public boolean isAdmin(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) return false;
        try {
            Long userId = Long.parseLong(userIdHeader.trim());
            return userRepository.findById(userId)
                .map(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .orElse(false);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Standard 403 response returned when admin check fails.
     */
    public <T> ResponseEntity<T> forbidden() {
        return ResponseEntity.status(403).build();
    }
}
