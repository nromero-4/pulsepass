package com.pulsepass.pulsepass.exception;

import com.pulsepass.pulsepass.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.pulsepass.pulsepass.security.SecurityRequestContextFilter;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage() == null
                        ? "Invalid value" : error.getDefaultMessage()));
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException exception) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Malformed request body", Map.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return errorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(AuthenticationException exception) {
        LOGGER.warn("event=auth.login.failure requestId={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY));
        return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid username/email or password", Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
        LOGGER.warn("event=security.access_denied requestId={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY));
        return errorResponse(HttpStatus.FORBIDDEN,
                "You do not have permission to perform this operation", Map.of());
    }

    @ExceptionHandler({DuplicateResourceException.class, BusinessRuleException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException exception) {
        return errorResponse(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("event=api.unexpected_error requestId={} exceptionType={}",
                MDC.get(SecurityRequestContextFilter.REQUEST_ID_MDC_KEY),
                exception.getClass().getSimpleName());
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", Map.of());
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message,
                                                          Map<String, String> details) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, details);
        return ResponseEntity.status(status).body(body);
    }
}