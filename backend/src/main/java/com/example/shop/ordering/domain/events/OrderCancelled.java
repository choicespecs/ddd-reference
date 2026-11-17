package com.example.shop.ordering.domain.events;

import com.example.shop.ordering.domain.OrderId;
import com.example.shop.sharedkernel.DomainEvent;

import java.time.Instant;
import java.util.List;

public record OrderCancelled(OrderId orderId, List<OrderPlaced.Line> lines, Instant occurredAt) implements DomainEvent {}
