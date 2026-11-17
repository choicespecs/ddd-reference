package com.example.shop.catalog.web;

import com.example.shop.catalog.application.CatalogService;
import com.example.shop.catalog.application.CreateProductCommand;
import com.example.shop.catalog.application.ProductView;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Value;
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
 * Request DTOs are nested immutable classes so the HTTP contract lives next to the endpoint that
 * uses it. Jackson builds them through Lombok's constructor (see backend/lombok.config).
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
                req.getSku(), req.getName(), req.getDescription(), req.getPrice(), req.getCurrency(),
                req.getInitialStock()));
    }

    @PutMapping("/{id}/price")
    ProductView changePrice(@PathVariable UUID id, @Valid @RequestBody ChangePriceRequest req) {
        return catalog.changePrice(id, req.getPrice(), req.getCurrency());
    }

    @PostMapping("/{id}/stock")
    ProductView receiveStock(@PathVariable UUID id, @Valid @RequestBody ReceiveStockRequest req) {
        return catalog.receiveStock(id, req.getQuantity());
    }

    @Value
    static class CreateProductRequest {
        @NotBlank @Size(max = 32) String sku;
        @NotBlank @Size(max = 200) String name;
        @Size(max = 2000) String description;
        @NotNull @DecimalMin("0.00") BigDecimal price;
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency;
        @Min(0) int initialStock;
    }

    @Value
    static class ChangePriceRequest {
        @NotNull @DecimalMin("0.00") BigDecimal price;
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency;
    }

    @Value
    static class ReceiveStockRequest {
        @Min(1) int quantity;
    }
}
