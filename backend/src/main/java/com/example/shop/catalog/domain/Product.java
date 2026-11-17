package com.example.shop.catalog.domain;

import com.example.shop.sharedkernel.AggregateRoot;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root of the Catalog context.
 *
 * <p>Rules this class protects (its invariants):
 * <ul>
 *   <li>stock on hand is never negative;</li>
 *   <li>an inactive product cannot be reserved;</li>
 *   <li>name is non-blank.</li>
 * </ul>
 *
 * <p>JPA notes: no public setters; a protected no-arg constructor exists only for Hibernate;
 * all state changes go through intention-revealing methods.
 */
@Entity
@Table(name = "catalog_product")
public class Product extends AggregateRoot {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 32)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Column(name = "stock_on_hand", nullable = false)
    private int stockOnHand;

    @Column(nullable = false)
    private boolean active;

    protected Product() {
        // for JPA
    }

    private Product(ProductId id, Sku sku, String name, String description, Money price, int initialStock) {
        this.id = id.value();
        this.sku = sku.value();
        rename(name);
        this.description = description;
        this.price = Objects.requireNonNull(price, "price");
        if (initialStock < 0) throw new DomainException("Initial stock cannot be negative");
        this.stockOnHand = initialStock;
        this.active = true;
    }

    /** Factory method: the only way to create a new product. */
    public static Product create(Sku sku, String name, String description, Money price, int initialStock) {
        return new Product(ProductId.newId(), sku, name, description, price, initialStock);
    }

    // ---- behaviour -------------------------------------------------------

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) throw new DomainException("Product name is required");
        this.name = newName.strip();
    }

    public void changePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "newPrice");
    }

    public void receiveStock(int quantity) {
        if (quantity <= 0) throw new DomainException("Received quantity must be positive");
        this.stockOnHand += quantity;
    }

    /** Called when an order is placed. Enforces "never oversell". */
    public void reserve(int quantity) {
        if (!active) throw new DomainException("Product " + sku + " is not available");
        if (quantity <= 0) throw new DomainException("Reserved quantity must be positive");
        if (quantity > stockOnHand) {
            throw new DomainException("Insufficient stock for " + sku + ": requested " + quantity + ", available " + stockOnHand);
        }
        this.stockOnHand -= quantity;
    }

    /** Compensating action when an order is cancelled. */
    public void release(int quantity) {
        if (quantity <= 0) throw new DomainException("Released quantity must be positive");
        this.stockOnHand += quantity;
    }

    public void deactivate() {
        this.active = false;
    }

    // ---- read accessors (no setters) -------------------------------------

    public ProductId id() { return new ProductId(id); }
    public Sku sku() { return new Sku(sku); }
    public String name() { return name; }
    public String description() { return description; }
    public Money price() { return price; }
    public int stockOnHand() { return stockOnHand; }
    public boolean isActive() { return active; }
}
