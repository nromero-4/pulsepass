package com.pulsepass.pulsepass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityRequestContextFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityRequestContextFilter.class);

    private final SecurityErrorResponseWriter responseWriter;
    private final boolean requireHttps;

    public SecurityRequestContextFilter(SecurityErrorResponseWriter responseWriter,
                                        org.springframework.core.env.Environment environment) {
        this.responseWriter = responseWriter;
        this.requireHttps = environment.getProperty("pulsepass.security.require-https", Boolean.class, false);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = validRequestId(request.getHeader(REQUEST_ID_HEADER))
                .orElseGet(() -> UUID.randomUUID().toString());
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            if (requireHttps && !request.isSecure() && !isLocalDevelopmentRequest(request)) {
                LOGGER.warn("event=security.insecure_transport requestId={}", requestId);
                responseWriter.write(response, HttpStatus.BAD_REQUEST, "HTTPS is required");
                return;
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private java.util.Optional<String> validRequestId(String value) {
        if (value == null) {
            return java.util.Optional.empty();
        }
        try {
            UUID requestId = UUID.fromString(value);
            String canonical = requestId.toString();
            return canonical.equalsIgnoreCase(value)
                    ? java.util.Optional.of(canonical)
                    : java.util.Optional.empty();
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }

    private boolean isLocalDevelopmentRequest(HttpServletRequest request) {
        String serverName = request.getServerName();
        return ("localhost".equalsIgnoreCase(serverName)
                || "127.0.0.1".equals(serverName)
                || "::1".equals(serverName)
                || "[::1]".equals(serverName))
                && isLoopbackAddress(request.getRemoteAddr());
    }

    private boolean isLoopbackAddress(String address) {
        if (address == null || !address.matches("[0-9a-fA-F:.%]+")) {
            return false;
        }
        try {
            return InetAddress.getByName(address).isLoopbackAddress();
        } catch (UnknownHostException exception) {
            return false;
        }
    }
}
