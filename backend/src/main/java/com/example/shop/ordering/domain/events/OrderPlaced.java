package com.example.shop.ordering.domain.events;

import com.example.shop.ordering.domain.OrderId;
import com.example.shop.sharedkernel.DomainEvent;
import com.example.shop.sharedkernel.Money;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain event: past tense, immutable, uses the context's own rich types (OrderId, Money).
 * Internal to Ordering — translated to {@code OrderPlacedIntegrationEvent} before leaving.
 */
public record OrderPlaced(OrderId orderId, List<Line> lines, Money total, Instant occurredAt) implements DomainEvent {
    public record Line(UUID productId, int quantity) {}
}
