package com.pulsepass.pulsepass.dto.response;

import com.pulsepass.pulsepass.enums.EventCategory;
import com.pulsepass.pulsepass.enums.EventStatus;
import java.time.LocalDate;
import java.util.List;

public record EventResponse(
        Long id,
        String eventCode,
        String name,
        String description,
        EventCategory category,
        EventStatus status,
        LocalDate eventDate,
        Integer minimumAge,
        String venueCode,
        String venueName,
        List<String> artists
) {
}
