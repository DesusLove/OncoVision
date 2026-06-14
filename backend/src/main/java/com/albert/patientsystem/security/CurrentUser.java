package com.albert.patientsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tiny helper for grabbing the currently-authenticated username inside
 * controllers and services without dragging in the full Authentication
 * object. Returns null if no user is logged in (shouldn't happen on
 * authenticated endpoints, but keeps callers safe).
 */
public final class CurrentUser {
    private CurrentUser() {}

    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    public static boolean isAuthenticated() {
        return username() != null;
    }
}
