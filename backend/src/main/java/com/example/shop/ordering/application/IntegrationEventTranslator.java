package com.example.shop.ordering.application;

import com.example.shop.integration.OrderCancelledIntegrationEvent;
import com.example.shop.integration.OrderPlacedIntegrationEvent;
import com.example.shop.ordering.domain.events.OrderCancelled;
import com.example.shop.ordering.domain.events.OrderPlaced;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns Ordering's internal domain events into public integration events (published language).
 * Keeps the domain model free to change without breaking consumers in other contexts.
 */
@Component
class IntegrationEventTranslator {

    private final ApplicationEventPublisher publisher;

    IntegrationEventTranslator(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    void on(OrderPlaced e) {
        publisher.publishEvent(new OrderPlacedIntegrationEvent(e.getOrderId().getValue(), toLines(e.getLines())));
    }

    @EventListener
    void on(OrderCancelled e) {
        publisher.publishEvent(new OrderCancelledIntegrationEvent(e.getOrderId().getValue(), toLines(e.getLines())));
    }

    private static List<OrderPlacedIntegrationEvent.Line> toLines(List<OrderPlaced.Line> lines) {
        return lines.stream()
                .map(l -> new OrderPlacedIntegrationEvent.Line(l.getProductId(), l.getQuantity()))
                .collect(Collectors.toList());
    }
}
