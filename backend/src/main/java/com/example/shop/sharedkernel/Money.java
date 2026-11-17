package com.example.shop.sharedkernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object: immutable, compared by value, validates itself on construction.
 * Mapped by JPA as an {@code @Embeddable} so it lives in the owning entity's table
 * (columns {@code *_amount} and {@code *_currency}).
 *
 * <p>Part of the shared kernel because both Catalog and Ordering speak "money".
 */
@Embeddable
public record Money(
        @Column(name = "amount", nullable = false, precision = 19, scale = 2) BigDecimal amount,
        @Column(name = "currency", nullable = false, length = 3) String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        Currency.getInstance(currency); // throws on an unknown ISO code
        if (amount.signum() < 0) {
            throw new DomainException("Money cannot be negative: " + amount);
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money times(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new DomainException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
