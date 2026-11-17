package com.example.shop.ordering.domain.events;

import com.example.shop.ordering.domain.OrderId;
import com.example.shop.sharedkernel.DomainEvent;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class OrderCancelled implements DomainEvent {
    OrderId orderId;
    List<OrderPlaced.Line> lines;
    Instant occurredAt;
}
