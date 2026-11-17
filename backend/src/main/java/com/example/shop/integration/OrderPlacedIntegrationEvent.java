package com.example.shop.integration;

import java.util.List;
import java.util.UUID;

/**
 * <b>Published language</b> between bounded contexts. Ordering publishes this; Catalog consumes it.
 *
 * <p>Why not let Catalog listen to Ordering's domain event directly? Because then Catalog would
 * depend on Ordering's internal model (and Ordering already depends on Catalog for prices →
 * a package cycle). Integration events are small, stable, primitive-typed contracts that are
 * safe to share — and are exactly what you would put on Kafka/RabbitMQ if the contexts were
 * ever split into separate services.
 */
public record OrderPlacedIntegrationEvent(UUID orderId, List<Line> lines) {
    public record Line(UUID productId, int quantity) {}
}
