package com.example.shop.ordering.domain;

import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
public class OrderId {
    UUID value;

    public OrderId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
