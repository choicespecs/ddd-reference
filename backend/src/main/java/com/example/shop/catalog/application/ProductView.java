package com.example.shop.catalog.application;

import com.example.shop.catalog.domain.Product;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model returned to callers (the REST layer and other contexts). Flat, primitive-typed,
 * no behaviour — safe to serialize to JSON and safe to hand to another bounded context.
 */
public record ProductView(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        int stockOnHand,
        boolean active) {

    static ProductView from(Product p) {
        return new ProductView(
                p.id().value(), p.sku().value(), p.name(), p.description(),
                p.price().amount(), p.price().currency(), p.stockOnHand(), p.isActive());
    }
}
