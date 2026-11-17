package com.example.shop.ordering.infrastructure;

import com.example.shop.catalog.application.CatalogService;
import com.example.shop.catalog.application.ProductView;
import com.example.shop.ordering.application.ProductCatalog;
import com.example.shop.sharedkernel.Money;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * <b>Anti-corruption layer.</b> The only class in Ordering allowed to import Catalog types.
 * It translates Catalog's {@link ProductView} into Ordering's own {@link ProductCatalog.OrderableProduct},
 * so Catalog can rename/restructure its model without rippling into Ordering's domain.
 */
@Component
class CatalogAdapter implements ProductCatalog {

    private final CatalogService catalog;

    CatalogAdapter(CatalogService catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<OrderableProduct> find(UUID productId) {
        return catalog.findProduct(productId).map(CatalogAdapter::translate);
    }

    private static OrderableProduct translate(ProductView p) {
        boolean available = p.active() && p.stockOnHand() > 0;
        return new OrderableProduct(p.id(), p.name(), new Money(p.price(), p.currency()), available);
    }
}
