package com.smg.productservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductCreatedEvent(
        UUID productId,
        String name,
        BigDecimal price,
        String occurredAt
) {
}