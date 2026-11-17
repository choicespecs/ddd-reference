package com.example.shop.ordering.domain;

import com.example.shop.ordering.domain.events.OrderCancelled;
import com.example.shop.ordering.domain.events.OrderPlaced;
import com.example.shop.sharedkernel.AggregateRoot;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root of the Ordering context. The aggregate = Order + its OrderLines.
 * Everything outside talks to the root only; the root guarantees these invariants:
 *
 * <ol>
 *   <li>Lines can only be changed while the order is DRAFT.</li>
 *   <li>An order must have at least one line to be placed.</li>
 *   <li>All lines share one currency (so the total is well-defined).</li>
 *   <li>At most {@value #MAX_LINES} lines; per-line quantity 1..{@value #MAX_QUANTITY_PER_LINE}.</li>
 *   <li>Status transitions follow {@link OrderStatus}' diagram.</li>
 * </ol>
 */
@Entity
@Table(name = "ordering_order")
public class Order extends AggregateRoot {

    public static final int MAX_LINES = 20;
    public static final int MAX_QUANTITY_PER_LINE = 99;

    @Id
    private UUID id;

    @Column(name = "customer_email", nullable = false, length = 320)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderStatus status;

    /*
     * Unidirectional one-to-many owned by the root. cascade=ALL + orphanRemoval=true means the
     * lines' lifecycle is entirely managed through the root: removing a line from this list
     * deletes its row. EAGER is acceptable here because an Order is always loaded as a whole
     * (aggregates are small by design); see docs → Persistence for the trade-offs.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @OrderBy("productName")
    private List<OrderLine> lines = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "placed_at")
    private Instant placedAt;

    protected Order() {
        // for JPA
    }

    private Order(OrderId id, CustomerEmail customer, Instant now) {
        this.id = id.value();
        this.customerEmail = customer.value();
        this.status = OrderStatus.DRAFT;
        this.createdAt = now;
    }

    public static Order startDraft(CustomerEmail customer, Clock clock) {
        return new Order(OrderId.newId(), customer, clock.instant());
    }

    // ---- behaviour -------------------------------------------------------

    /**
     * Adds a product, or increases the quantity if it is already on the order.
     * Price and name are passed in (snapshotted) — the aggregate never calls out to Catalog.
     */
    public void addLine(UUID productId, String productName, Money unitPrice, int quantity) {
        requireStatus(OrderStatus.DRAFT, "add lines");
        Optional<OrderLine> existing = findLine(productId);
        if (existing.isPresent()) {
            existing.get().changeQuantity(existing.get().quantity() + quantity);
            return;
        }
        if (lines.size() >= MAX_LINES) {
            throw new DomainException("An order can have at most " + MAX_LINES + " lines");
        }
        if (!lines.isEmpty() && !lines.get(0).unitPrice().currency().equals(unitPrice.currency())) {
            throw new DomainException("All lines must use " + lines.get(0).unitPrice().currency());
        }
        lines.add(new OrderLine(productId, productName, unitPrice, quantity));
    }

    public void changeQuantity(UUID productId, int quantity) {
        requireStatus(OrderStatus.DRAFT, "change quantities");
        line(productId).changeQuantity(quantity);
    }

    public void removeLine(UUID productId) {
        requireStatus(OrderStatus.DRAFT, "remove lines");
        lines.remove(line(productId));
    }

    public void place(Clock clock) {
        requireStatus(OrderStatus.DRAFT, "place the order");
        if (lines.isEmpty()) {
            throw new DomainException("Cannot place an empty order");
        }
        this.status = OrderStatus.PLACED;
        this.placedAt = clock.instant();
        registerEvent(new OrderPlaced(id(), snapshotLines(), total(), placedAt));
    }

    public void ship() {
        requireStatus(OrderStatus.PLACED, "ship");
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel(Clock clock) {
        switch (status) {
            case DRAFT -> status = OrderStatus.CANCELLED;
            case PLACED -> {
                status = OrderStatus.CANCELLED;
                registerEvent(new OrderCancelled(id(), snapshotLines(), clock.instant()));
            }
            case SHIPPED -> throw new DomainException("A shipped order cannot be cancelled");
            case CANCELLED -> throw new DomainException("Order is already cancelled");
        }
    }

    // ---- derived state ---------------------------------------------------

    public Money total() {
        if (lines.isEmpty()) return Money.zero("USD");
        Money sum = Money.zero(lines.get(0).unitPrice().currency());
        for (OrderLine line : lines) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    // ---- helpers ---------------------------------------------------------

    private void requireStatus(OrderStatus required, String action) {
        if (status != required) {
            throw new DomainException("Cannot " + action + " when order is " + status);
        }
    }

    private Optional<OrderLine> findLine(UUID productId) {
        return lines.stream().filter(l -> l.productId().equals(productId)).findFirst();
    }

    private OrderLine line(UUID productId) {
        return findLine(productId).orElseThrow(
                () -> new DomainException("Product " + productId + " is not on this order"));
    }

    private List<OrderPlaced.Line> snapshotLines() {
        return lines.stream().map(l -> new OrderPlaced.Line(l.productId(), l.quantity())).toList();
    }

    // ---- read accessors --------------------------------------------------

    public OrderId id() { return new OrderId(id); }
    public CustomerEmail customerEmail() { return new CustomerEmail(customerEmail); }
    public OrderStatus status() { return status; }
    public List<OrderLine> lines() { return Collections.unmodifiableList(lines); }
    public Instant createdAt() { return createdAt; }
    public Instant placedAt() { return placedAt; }
}
