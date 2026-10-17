package com.pulsepass.pulsepass.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.pulsepass.config.SecurityProperties;
import com.pulsepass.pulsepass.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    @Test
    void generatedTokenUsesPersistentUserIdAndCanBeValidated() {
        JwtService jwtService = new JwtService(properties("pulsepass-api"));
        User user = user(42L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(900L);
    }

    @Test
    void rejectsTokenIssuedByAnotherIssuer() {
        JwtService expectedIssuer = new JwtService(properties("pulsepass-api"));
        JwtService otherIssuer = new JwtService(properties("untrusted-issuer"));
        String token = otherIssuer.generateToken(user(42L));

        assertThat(expectedIssuer.isTokenValid(token)).isFalse();
    }

    @Test
    void rejectsTamperedToken() {
        JwtService jwtService = new JwtService(properties("pulsepass-api"));
        String token = jwtService.generateToken(user(42L));
        String[] parts = token.split("\\.");
        String replacement = parts[2].startsWith("A") ? "B" : "A";
        String tampered = parts[0] + "." + parts[1] + "." + replacement + parts[2].substring(1);

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void rejectsEmptyAndMalformedTokensWithoutThrowing() {
        JwtService jwtService = new JwtService(properties("pulsepass-api"));

        assertThat(jwtService.isTokenValid("")).isFalse();
        assertThat(jwtService.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        SecurityProperties properties = properties("pulsepass-api");
        properties.setAccessTokenTtlSeconds(1);
        JwtService jwtService = new JwtService(properties);

        String token = jwtService.generateToken(user(42L));
        try {
            Thread.sleep(1100);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for JWT expiration", exception);
        }
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void rejectsTokenIssuedInTheFuture() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TestJwtSecret.base64()));
        Date future = new Date(System.currentTimeMillis() + 60_000);
        String token = Jwts.builder()
                .issuer("pulsepass-api")
                .subject("42")
                .issuedAt(future)
                .expiration(new Date(future.getTime() + 60_000))
                .id("future-issued-token")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertThat(new JwtService(properties("pulsepass-api")).isTokenValid(token)).isFalse();
    }

    @Test
    void failsConstructionWhenSecretIsMissingOrTooShort() {
        SecurityProperties properties = properties("pulsepass-api");
        properties.setSecretBase64("");

        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");

        properties.setSecretBase64("c2hvcnQ=");
        assertThatThrownBy(() -> new JwtService(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void failsConstructionWhenIssuerOrTokenLifetimeIsInvalid() {
        SecurityProperties blankIssuer = properties(" ");
        assertThatThrownBy(() -> new JwtService(blankIssuer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer");

        SecurityProperties invalidLifetime = properties("pulsepass-api");
        invalidLifetime.setAccessTokenTtlSeconds(-1);
        assertThatThrownBy(() -> new JwtService(invalidLifetime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TTL");
    }

    private SecurityProperties properties(String issuer) {
        SecurityProperties properties = new SecurityProperties();
        properties.setIssuer(issuer);
        properties.setSecretBase64(TestJwtSecret.base64());
        return properties;
    }

    private User user(Long id) {
        User user = new User("andrea", "andrea@example.com");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
