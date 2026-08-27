package com.quickbite.common.security;

public final class SecurityConstants {
    private SecurityConstants() {}

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    
    public static final String DEFAULT_SECRET_KEY = "QuickBiteSuperSecretKeyForJwtAuthenticationAndAuthorizationPurposes2026";
    public static final long ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L; // 24 hours
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days
}
