package com.example.shop.integration;

import lombok.Value;

import java.util.List;
import java.util.UUID;

/** Published when a previously placed order is cancelled, so reserved stock can be released. */
@Value
public class OrderCancelledIntegrationEvent {
    UUID orderId;
    List<OrderPlacedIntegrationEvent.Line> lines;
}
