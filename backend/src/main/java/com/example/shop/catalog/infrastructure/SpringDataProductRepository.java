package com.example.shop.catalog.infrastructure;

import com.example.shop.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data interface; package-private so nothing outside infrastructure can bypass the port. */
interface SpringDataProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    @Query("select p from Product p where p.active = true order by p.name")
    List<Product> findAllActive();
}
