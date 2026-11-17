package com.example.shop.ordering.infrastructure;

import com.example.shop.ordering.domain.Order;
import com.example.shop.ordering.domain.OrderId;
import com.example.shop.ordering.domain.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository jpa;

    JpaOrderRepository(SpringDataOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpa.findById(id.value());
    }

    @Override
    public List<Order> findRecent(int limit) {
        return jpa.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Override
    public Order save(Order order) {
        return jpa.save(order);
    }
}
