package com.example.shop.ordering.application;

import com.example.shop.sharedkernel.Money;

import java.util.Optional;
import java.util.UUID;

/**
 * Port owned by Ordering describing what <em>Ordering</em> needs from a catalog, in Ordering's words.
 * Implemented by {@code ordering.infrastructure.CatalogAdapter} (the anti-corruption layer).
 * If Catalog became a separate microservice, only the adapter would change (HTTP client instead
 * of a method call) — the application and domain code would not.
 */
public interface ProductCatalog {

    Optional<OrderableProduct> find(UUID productId);

    record OrderableProduct(UUID productId, String name, Money price, boolean available) {}
}
