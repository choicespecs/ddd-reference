package com.example.shop.catalog.infrastructure;

import com.example.shop.catalog.domain.Product;
import com.example.shop.catalog.domain.ProductId;
import com.example.shop.catalog.domain.ProductRepository;
import com.example.shop.catalog.domain.Sku;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Adapter: implements the domain's repository port using Spring Data JPA. */
@Repository
class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository jpa;

    JpaProductRepository(SpringDataProductRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jpa.findById(id.getValue());
    }

    @Override
    public Optional<Product> findBySku(Sku sku) {
        return jpa.findBySku(sku.getValue());
    }

    @Override
    public List<Product> findAllActive() {
        return jpa.findAllActive();
    }

    @Override
    public Product save(Product product) {
        return jpa.save(product);
    }
}
