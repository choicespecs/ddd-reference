package com.example.shop.catalog.web;

import com.example.shop.catalog.application.CatalogService;
import com.example.shop.catalog.application.CreateProductCommand;
import com.example.shop.catalog.application.ProductView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REST adapter for the Catalog context. Thin: translate HTTP ⇄ commands/views, nothing else.
 * Request DTOs are nested records so the HTTP contract lives next to the endpoint that uses it.
 */
@RestController
@RequestMapping("/api/products")
class ProductController {

    private final CatalogService catalog;

    ProductController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    List<ProductView> list() {
        return catalog.listActiveProducts();
    }

    @GetMapping("/{id}")
    ProductView get(@PathVariable UUID id) {
        return catalog.getProduct(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProductView create(@Valid @RequestBody CreateProductRequest req) {
        return catalog.createProduct(new CreateProductCommand(
                req.sku(), req.name(), req.description(), req.price(), req.currency(), req.initialStock()));
    }

    @PutMapping("/{id}/price")
    ProductView changePrice(@PathVariable UUID id, @Valid @RequestBody ChangePriceRequest req) {
        return catalog.changePrice(id, req.price(), req.currency());
    }

    @PostMapping("/{id}/stock")
    ProductView receiveStock(@PathVariable UUID id, @Valid @RequestBody ReceiveStockRequest req) {
        return catalog.receiveStock(id, req.quantity());
    }

    record CreateProductRequest(
            @NotBlank @Size(max = 32) String sku,
            @NotBlank @Size(max = 200) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
            @Min(0) int initialStock) {}

    record ChangePriceRequest(@NotNull @DecimalMin("0.00") BigDecimal price,
                              @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {}

    record ReceiveStockRequest(@Min(1) int quantity) {}
}
