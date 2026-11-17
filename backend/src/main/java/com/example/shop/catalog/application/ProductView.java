package com.example.shop.catalog.application;

import com.example.shop.catalog.domain.Product;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read model returned to callers (the REST layer and other contexts). Flat, primitive-typed,
 * no behaviour — safe to serialize to JSON and safe to hand to another bounded context.
 * Jackson serializes Lombok's getters: getId() → "id", isActive() → "active", etc.
 */
@Value
public class ProductView {
    UUID id;
    String sku;
    String name;
    String description;
    BigDecimal price;
    String currency;
    int stockOnHand;
    boolean active;

    static ProductView from(Product p) {
        return new ProductView(
                p.getId().getValue(), p.getSku().getValue(), p.getName(), p.getDescription(),
                p.getPrice().getAmount(), p.getPrice().getCurrency(), p.getStockOnHand(), p.isActive());
    }
}
