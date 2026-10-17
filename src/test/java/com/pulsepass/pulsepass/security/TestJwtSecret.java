package com.pulsepass.pulsepass.security;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.test.context.DynamicPropertyRegistry;

public final class TestJwtSecret {

    private static final String SECRET_BASE64 = generateSecret();

    private TestJwtSecret() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("pulsepass.security.jwt.secret-base64", () -> SECRET_BASE64);
    }

    public static String base64() {
        return SECRET_BASE64;
    }

    private static String generateSecret() {
        byte[] secret = new byte[32];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }
}
