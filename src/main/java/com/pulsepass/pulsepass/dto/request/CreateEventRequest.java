package com.pulsepass.pulsepass.dto.request;

import com.pulsepass.pulsepass.enums.EventCategory;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank
        String eventCode,
        @NotBlank
        String name,
        @NotBlank
        @Size(max = 1000)
        String description,
        @NotNull
        EventCategory category,
        @NotNull
        LocalDateTime eventDate,
        @NotNull
        @Min(0)
        Integer minimumAge,
        @NotBlank
        String venueCode
) {
}
