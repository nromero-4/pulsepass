package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TicketPricingStrategy {
    public BigDecimal priceFor(TicketType type) {
        if (type == null) {
            throw new BusinessRuleException("Ticket type is required to calculate its price.");
        }
        return switch (type) {
            case GENERAL -> new BigDecimal("100000.00");
            case STUDENT -> new BigDecimal("80000.00");
            case VIP -> new BigDecimal("250000.00");
            case BACKSTAGE -> new BigDecimal("400000.00");
        };
    }
}