package com.example.shop.ordering.application;

import lombok.Value;

import java.util.UUID;

/** Input classes for Ordering use cases. Grouped in one file because each is tiny. */
public final class Commands {
    private Commands() {}

    @Value
    public static class StartOrder {
        String customerEmail;
    }

    @Value
    public static class AddLine {
        UUID orderId;
        UUID productId;
        int quantity;
    }

    @Value
    public static class ChangeQuantity {
        UUID orderId;
        UUID productId;
        int quantity;
    }

    @Value
    public static class RemoveLine {
        UUID orderId;
        UUID productId;
    }
}
