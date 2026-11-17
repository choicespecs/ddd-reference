package com.example.shop.ordering.domain;

import com.example.shop.sharedkernel.DomainException;
import lombok.Value;

import java.util.Locale;
import java.util.regex.Pattern;

/** Value object that normalizes and validates an email address. */
@Value
public class CustomerEmail {
    private static final Pattern SIMPLE_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    String value;

    public CustomerEmail(String value) {
        if (value == null || !SIMPLE_EMAIL.matcher(value.strip()).matches()) {
            throw new DomainException("Invalid customer email: " + value);
        }
        this.value = value.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return value;
    }
}
