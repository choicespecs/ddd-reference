package com.example.shop.catalog.application;

import com.example.shop.integration.OrderCancelledIntegrationEvent;
import com.example.shop.integration.OrderPlacedIntegrationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Catalog reacts to Ordering's integration events.
 *
 * <p>{@code @EventListener} runs <em>synchronously in the publisher's transaction</em>, so if
 * stock is insufficient the DomainException rolls back the whole "place order" transaction —
 * strong consistency across two aggregates. That is a deliberate, documented trade-off
 * (see docs → Domain events). For eventual consistency switch to
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} + an outbox.
 */
@Component
class StockReservationHandler {

    private final CatalogService catalog;

    StockReservationHandler(CatalogService catalog) {
        this.catalog = catalog;
    }

    @EventListener
    void on(OrderPlacedIntegrationEvent event) {
        event.getLines().forEach(line -> catalog.reserveStock(line.getProductId(), line.getQuantity()));
    }

    @EventListener
    void on(OrderCancelledIntegrationEvent event) {
        event.getLines().forEach(line -> catalog.releaseStock(line.getProductId(), line.getQuantity()));
    }
}
