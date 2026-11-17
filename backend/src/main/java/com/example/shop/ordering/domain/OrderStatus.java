package com.example.shop.ordering.domain;

/**
 * Lifecycle of an order. Allowed transitions are enforced by {@link Order}, not here:
 * <pre>
 *   DRAFT ──place()──▶ PLACED ──ship()──▶ SHIPPED
 *     │                  │
 *     └──cancel()──▶ CANCELLED ◀──cancel()──┘
 * </pre>
 */
public enum OrderStatus {
    DRAFT, PLACED, SHIPPED, CANCELLED
}
