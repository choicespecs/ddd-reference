package com.example.shop.ordering.domain;

import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity <em>inside</em> the Order aggregate. It has an identity, but only a local one:
 * nothing outside the aggregate holds a reference to an OrderLine, and it is only ever
 * changed through {@link Order}. Hence package-private mutators.
 *
 * <p>Note {@code productName} and {@code unitPrice} are <b>snapshots</b> taken when the line
 * was added. Ordering does not hold a live reference to Catalog's Product — if the catalog
 * price changes tomorrow, yesterday's order must not change.
 */
@Entity
@Table(name = "ordering_order_line")
public class OrderLine {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    @Column(nullable = false)
    private int quantity;

    protected OrderLine() {
        // for JPA
    }

    OrderLine(UUID productId, String productName, Money unitPrice, int quantity) {
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        changeQuantity(quantity);
    }

    void changeQuantity(int newQuantity) {
        if (newQuantity < 1 || newQuantity > Order.MAX_QUANTITY_PER_LINE) {
            throw new DomainException("Quantity must be between 1 and " + Order.MAX_QUANTITY_PER_LINE);
        }
        this.quantity = newQuantity;
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    public UUID id() { return id; }
    public UUID productId() { return productId; }
    public String productName() { return productName; }
    public Money unitPrice() { return unitPrice; }
    public int quantity() { return quantity; }
}
