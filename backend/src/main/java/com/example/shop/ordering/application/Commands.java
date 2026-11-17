package com.example.shop.ordering.application;

import java.util.UUID;

/** Input records for Ordering use cases. Grouped in one file because each is a one-liner. */
public final class Commands {
    private Commands() {}

    public record StartOrder(String customerEmail) {}
    public record AddLine(UUID orderId, UUID productId, int quantity) {}
    public record ChangeQuantity(UUID orderId, UUID productId, int quantity) {}
    public record RemoveLine(UUID orderId, UUID productId) {}
}
