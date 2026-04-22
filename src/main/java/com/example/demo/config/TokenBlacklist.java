package com.example.demo.config;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * In-memory token blacklist.
 * When a user logs out, their JWT is added here so it can never be reused
 * — even if it's still valid and present in the browser cookie or URL.
 */
@Component
public class TokenBlacklist {

    // Thread-safe set — multiple requests may hit this concurrently
    private final Set<String> blacklistedTokens =
            Collections.synchronizedSet(new HashSet<>());

    /** Add a token to the blacklist (called on logout). */
    public void blacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklistedTokens.add(token);
        }
    }

    /** Returns true if the token has been revoked. */
    public boolean isBlacklisted(String token) {
        return token != null && blacklistedTokens.contains(token);
    }
}
