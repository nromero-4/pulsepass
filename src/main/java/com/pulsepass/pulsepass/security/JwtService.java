package com.pulsepass.pulsepass.security;

import com.pulsepass.pulsepass.config.SecurityProperties;
import com.pulsepass.pulsepass.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecurityProperties properties;
    private final SecretKey signingKey;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
        if (properties.getIssuer() == null || properties.getIssuer().isBlank()) {
            throw new IllegalStateException("JWT issuer is not configured");
        }
        if (properties.getAccessTokenTtlSeconds() <= 0) {
            throw new IllegalStateException("JWT access token TTL must be greater than zero");
        }
        this.signingKey = createSigningKey(properties.getSecretBase64());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + properties.getAccessTokenTtlSeconds() * 1000L);

        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiration)
                .id(UUID.randomUUID().toString())
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Long extractUserId(String token) {
        try {
            return Long.valueOf(parseClaims(token).getSubject());
        } catch (NumberFormatException exception) {
            throw new MalformedJwtException("JWT subject must be a user ID", exception);
        }
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            if (claims.getSubject() == null || claims.getSubject().isBlank()
                    || claims.getId() == null || claims.getId().isBlank()
                    || claims.getIssuedAt() == null
                    || claims.getIssuedAt().after(new Date())
                    || claims.getExpiration() == null
                    || !claims.getExpiration().after(new Date())) {
                return false;
            }
            Long.valueOf(claims.getSubject());
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return properties.getAccessTokenTtlSeconds();
    }

    private Claims parseClaims(String token) {
        var parsed = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token);
        if (!Jwts.SIG.HS256.getId().equals(parsed.getHeader().getAlgorithm())) {
            throw new MalformedJwtException("JWT algorithm must be HS256");
        }
        return parsed.getPayload();
    }

    private static SecretKey createSigningKey(String secretBase64) {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT secret must be a Base64-encoded key of at least 32 bytes");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT secret must be valid Base64", exception);
        }
    }
}
