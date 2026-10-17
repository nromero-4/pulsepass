package com.pulsepass.pulsepass.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    private final SecurityErrorResponseWriter responseWriter;

    public ApiAuthenticationEntryPoint(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        LOGGER.warn("event=security.authentication_required requestId={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY));
        responseWriter.write(response, HttpStatus.UNAUTHORIZED, "Authentication is required");
    }
}
