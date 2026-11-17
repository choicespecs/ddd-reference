package com.example.shop.catalog.domain;

import com.example.shop.sharedkernel.DomainException;
import lombok.Value;

import java.util.regex.Pattern;

/** Stock-keeping unit, e.g. {@code BOOK-0001}. A value object with a format rule. */
@Value
public class Sku {
    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{2,10}-\\d{4,8}$");

    String value;

    public Sku(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new DomainException("Invalid SKU '" + value + "' (expected e.g. BOOK-0001)");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
