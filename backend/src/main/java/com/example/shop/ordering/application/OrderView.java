package com.example.shop.ordering.application;

import com.example.shop.ordering.domain.Order;
import com.example.shop.ordering.domain.OrderLine;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Read model for one order, shaped for the UI. */
@Value
public class OrderView {
    UUID id;
    String customerEmail;
    String status;
    List<LineView> lines;
    BigDecimal total;
    String currency;
    Instant createdAt;
    Instant placedAt;
    long version;

    @Value
    public static class LineView {
        UUID productId;
        String productName;
        BigDecimal unitPrice;
        int quantity;
        BigDecimal lineTotal;

        static LineView from(OrderLine l) {
            return new LineView(l.getProductId(), l.getProductName(), l.getUnitPrice().getAmount(),
                    l.getQuantity(), l.lineTotal().getAmount());
        }
    }

    static OrderView from(Order o) {
        return new OrderView(
                o.getId().getValue(), o.getCustomerEmail().getValue(), o.getStatus().name(),
                o.getLines().stream().map(LineView::from).collect(Collectors.toList()),
                o.total().getAmount(), o.total().getCurrency(),
                o.getCreatedAt(), o.getPlacedAt(), o.getVersion());
    }
}
