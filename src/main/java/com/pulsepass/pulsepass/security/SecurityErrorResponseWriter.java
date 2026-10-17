package com.pulsepass.pulsepass.security;

import com.pulsepass.pulsepass.dto.response.ErrorResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(jakarta.servlet.http.HttpServletResponse response,
                      HttpStatus status,
                      String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                        message, Map.of()));
    }
}
