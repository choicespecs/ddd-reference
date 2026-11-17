package com.example.shop.catalog.domain;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier. Prevents passing an OrderId where a ProductId is expected.
 * Stored as a plain UUID column; the entity converts at its boundary.
 *
 * <p>{@code @Value} (Lombok) = final class, private final field, getter, equals/hashCode.
 * The explicit constructor below replaces Lombok's generated one so we can validate.
 */
@Value
public class ProductId {
    UUID value;

    public ProductId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId of(String raw) {
        return new ProductId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
