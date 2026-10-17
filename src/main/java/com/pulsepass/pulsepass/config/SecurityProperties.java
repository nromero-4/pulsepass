package com.pulsepass.pulsepass.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsepass.security.jwt")
public class SecurityProperties {
    private String issuer = "pulsepass-api";
    private String secretBase64;
    private long accessTokenTtlSeconds = 900;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSecretBase64() {
        return secretBase64;
    }

    public void setSecretBase64(String secretBase64) {
        this.secretBase64 = secretBase64;
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }
}
