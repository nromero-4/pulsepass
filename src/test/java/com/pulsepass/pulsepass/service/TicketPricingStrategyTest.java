package com.pulsepass.pulsepass.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pulsepass.pulsepass.enums.TicketType;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TicketPricingStrategyTest {
    private final TicketPricingStrategy pricingStrategy = new TicketPricingStrategy();

    @Test
    void calculatesPriceForEachTicketType() {
        assertThat(pricingStrategy.priceFor(TicketType.GENERAL)).isEqualByComparingTo("100000.00");
        assertThat(pricingStrategy.priceFor(TicketType.STUDENT)).isEqualByComparingTo("80000.00");
        assertThat(pricingStrategy.priceFor(TicketType.VIP)).isEqualByComparingTo("250000.00");
        assertThat(pricingStrategy.priceFor(TicketType.BACKSTAGE)).isEqualByComparingTo("400000.00");
    }

    @Test
    void allTicketPricesAreNonNegative() {
        assertThat(java.util.Arrays.stream(TicketType.values())
                .map(pricingStrategy::priceFor)
                .allMatch(price -> price.compareTo(BigDecimal.ZERO) >= 0))
                .isTrue();
    }

    @Test
    void rejectsMissingTicketType() {
        assertThatThrownBy(() -> pricingStrategy.priceFor(null))
                .isInstanceOf(BusinessRuleException.class);
    }
}