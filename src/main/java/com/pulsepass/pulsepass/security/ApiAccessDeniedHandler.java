package com.pulsepass.pulsepass.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    private final SecurityErrorResponseWriter responseWriter;

    public ApiAccessDeniedHandler(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException exception) throws IOException, ServletException {
        LOGGER.warn("event=security.access_denied requestId={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY));
        responseWriter.write(response, HttpStatus.FORBIDDEN, "You do not have permission to perform this operation");
    }
}
