package com.example.shop.integration;

import java.util.List;
import java.util.UUID;

/** Published when a previously placed order is cancelled, so reserved stock can be released. */
public record OrderCancelledIntegrationEvent(UUID orderId, List<OrderPlacedIntegrationEvent.Line> lines) {}
