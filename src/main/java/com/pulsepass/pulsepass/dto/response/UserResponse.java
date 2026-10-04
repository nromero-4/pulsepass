package com.pulsepass.pulsepass.dto.response;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String city,
        LocalDate birthDate,
        Boolean active
) {
}
