package com.example.shop.catalog.application;

import com.example.shop.catalog.domain.Product;
import com.example.shop.catalog.domain.ProductId;
import com.example.shop.catalog.domain.ProductRepository;
import com.example.shop.catalog.domain.Sku;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import com.example.shop.sharedkernel.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for the Catalog context: one public method per use case.
 * It orchestrates (load → call domain behaviour → save) and owns the transaction boundary,
 * but contains no business rules itself.
 *
 * <p>This class is also Catalog's public API for other contexts (see Ordering's CatalogAdapter).
 */
@Service
@Transactional
public class CatalogService {

    private final ProductRepository products;

    public CatalogService(ProductRepository products) {
        this.products = products;
    }

    public ProductView createProduct(CreateProductCommand cmd) {
        Sku sku = new Sku(cmd.sku());
        products.findBySku(sku).ifPresent(existing -> {
            throw new DomainException("SKU already exists: " + sku);
        });
        Product product = Product.create(sku, cmd.name(), cmd.description(),
                new Money(cmd.price(), cmd.currency()), cmd.initialStock());
        return ProductView.from(products.save(product));
    }

    public ProductView changePrice(UUID productId, BigDecimal amount, String currency) {
        Product product = load(productId);
        product.changePrice(new Money(amount, currency));
        return ProductView.from(product); // dirty checking flushes the change at commit
    }

    public ProductView receiveStock(UUID productId, int quantity) {
        Product product = load(productId);
        product.receiveStock(quantity);
        return ProductView.from(product);
    }

    public void reserveStock(UUID productId, int quantity) {
        load(productId).reserve(quantity);
    }

    public void releaseStock(UUID productId, int quantity) {
        load(productId).release(quantity);
    }

    @Transactional(readOnly = true)
    public List<ProductView> listActiveProducts() {
        return products.findAllActive().stream().map(ProductView::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductView getProduct(UUID productId) {
        return ProductView.from(load(productId));
    }

    @Transactional(readOnly = true)
    public Optional<ProductView> findProduct(UUID productId) {
        return products.findById(new ProductId(productId)).map(ProductView::from);
    }

    private Product load(UUID productId) {
        return products.findById(new ProductId(productId))
                .orElseThrow(() -> new NotFoundException("Product", productId));
    }
}
