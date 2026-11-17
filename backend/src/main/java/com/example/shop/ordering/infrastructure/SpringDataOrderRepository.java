package com.example.shop.ordering.infrastructure;

import com.example.shop.ordering.domain.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
