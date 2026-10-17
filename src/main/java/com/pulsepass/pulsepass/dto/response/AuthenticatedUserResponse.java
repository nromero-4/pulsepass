package com.pulsepass.pulsepass.dto.response;

import java.util.Set;

public record AuthenticatedUserResponse(
        Long userId,
        String username,
        String email,
        Set<String> roles
) {
}
