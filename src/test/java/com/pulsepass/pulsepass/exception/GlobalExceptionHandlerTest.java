package com.pulsepass.pulsepass.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.pulsepass.dto.response.ErrorResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void validationFailureReturnsBadRequestAndFieldDetails() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Email is required"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().details()).containsEntry("email", "Email is required");
    }

    @Test
    void malformedRequestReturnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleMalformedRequest(
            new HttpMessageNotReadableException("Malformed JSON", (HttpInputMessage) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Malformed request body");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void notFoundReturns404WithExceptionMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Event not found: EVT-1"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("Event not found: EVT-1");
        assertThat(response.getBody().details()).isEmpty();
    }

    @Test
    void duplicateResourceReturnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(
                new DuplicateResourceException("Email already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
    }

    @Test
    void businessRuleReturnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(
                new BusinessRuleException("Event is not published"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Event is not published");
    }

    @Test
    void unexpectedErrorReturnsSafeInternalServerErrorMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().message()).doesNotContain("internal detail");
        assertThat(response.getBody().timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}