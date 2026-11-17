package com.example.shop.ordering.domain;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    List<Order> findRecent(int limit);
    Order save(Order order);
}
