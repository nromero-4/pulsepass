package com.pulsepass.pulsepass.dto.request;

import com.pulsepass.pulsepass.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseTicketRequest(
        @NotBlank
        @Email
        String userEmail,
        @NotBlank
        String eventCode,
        @NotNull
        TicketType type
) {
}
