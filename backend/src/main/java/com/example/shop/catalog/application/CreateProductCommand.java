package com.example.shop.catalog.application;

import java.math.BigDecimal;

/** Input to the "create product" use case. Plain data; validation of rules happens in the domain. */
public record CreateProductCommand(String sku, String name, String description,
                                   BigDecimal price, String currency, int initialStock) {}
