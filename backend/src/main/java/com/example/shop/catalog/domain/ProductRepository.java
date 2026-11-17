package com.example.shop.catalog.domain;

import java.util.List;
import java.util.Optional;

/**
 * Repository <em>port</em>: defined by the domain in domain terms, implemented in
 * {@code catalog.infrastructure}. One repository per aggregate root.
 */
public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    Optional<Product> findBySku(Sku sku);
    List<Product> findAllActive();
    Product save(Product product);
}
