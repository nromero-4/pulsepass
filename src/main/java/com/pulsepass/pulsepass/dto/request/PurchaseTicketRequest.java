package com.pulsepass.pulsepass.dto.request;

import com.pulsepass.pulsepass.enums.TicketType;

public record PurchaseTicketRequest(
        String userEmail,
        String eventCode,
        TicketType type
) {
}
