package com.example.shop.catalog.domain;

import com.example.shop.sharedkernel.DomainException;

import java.util.regex.Pattern;

/** Stock-keeping unit, e.g. {@code BOOK-0001}. A value object with a format rule. */
public record Sku(String value) {
    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{2,10}-\\d{4,8}$");

    public Sku {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new DomainException("Invalid SKU '" + value + "' (expected e.g. BOOK-0001)");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
