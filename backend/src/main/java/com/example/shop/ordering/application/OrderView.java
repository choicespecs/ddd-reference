package com.example.shop.ordering.application;

import com.example.shop.ordering.domain.Order;
import com.example.shop.ordering.domain.OrderLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read model for one order, shaped for the UI. */
public record OrderView(
        UUID id,
        String customerEmail,
        String status,
        List<LineView> lines,
        BigDecimal total,
        String currency,
        Instant createdAt,
        Instant placedAt,
        long version) {

    public record LineView(UUID productId, String productName, BigDecimal unitPrice,
                           int quantity, BigDecimal lineTotal) {
        static LineView from(OrderLine l) {
            return new LineView(l.productId(), l.productName(), l.unitPrice().amount(),
                    l.quantity(), l.lineTotal().amount());
        }
    }

    static OrderView from(Order o) {
        return new OrderView(
                o.id().value(), o.customerEmail().value(), o.status().name(),
                o.lines().stream().map(LineView::from).toList(),
                o.total().amount(), o.total().currency(),
                o.createdAt(), o.placedAt(), o.version());
    }
}
