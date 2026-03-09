package com.example.e_commerce.security;

import com.example.e_commerce.user.repo.UserRepository;
import com.example.e_commerce.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting the currently authenticated user from the
 * SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Returns the email (username) of the currently authenticated user.
     * Throws IllegalStateException if called on an unauthenticated request.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return auth.getName();
    }

    /**
     * Returns the full User entity for the currently authenticated user.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found: " + email));
    }

    /**
     * Returns the ID of the currently authenticated user.
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
