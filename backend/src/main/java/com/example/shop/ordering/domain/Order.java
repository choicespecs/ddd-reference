package com.example.shop.ordering.domain;

import com.example.shop.ordering.domain.events.OrderCancelled;
import com.example.shop.ordering.domain.events.OrderPlaced;
import com.example.shop.sharedkernel.AggregateRoot;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        this.id = id.getValue();
        this.customerEmail = customer.getValue();
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
            existing.get().changeQuantity(existing.get().getQuantity() + quantity);
            return;
        }
        if (lines.size() >= MAX_LINES) {
            throw new DomainException("An order can have at most " + MAX_LINES + " lines");
        }
        String orderCurrency = lines.isEmpty() ? null : lines.get(0).getUnitPrice().getCurrency();
        if (orderCurrency != null && !orderCurrency.equals(unitPrice.getCurrency())) {
            throw new DomainException("All lines must use " + orderCurrency);
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
        registerEvent(new OrderPlaced(getId(), snapshotLines(), total(), placedAt));
    }

    public void ship() {
        requireStatus(OrderStatus.PLACED, "ship");
        this.status = OrderStatus.SHIPPED;
    }

    public void cancel(Clock clock) {
        switch (status) {
            case DRAFT:
                status = OrderStatus.CANCELLED;
                break;
            case PLACED:
                status = OrderStatus.CANCELLED;
                registerEvent(new OrderCancelled(getId(), snapshotLines(), clock.instant()));
                break;
            case SHIPPED:
                throw new DomainException("A shipped order cannot be cancelled");
            case CANCELLED:
                throw new DomainException("Order is already cancelled");
            default:
                throw new IllegalStateException("Unknown status " + status);
        }
    }

    // ---- derived state ---------------------------------------------------

    public Money total() {
        if (lines.isEmpty()) return Money.zero("USD");
        Money sum = Money.zero(lines.get(0).getUnitPrice().getCurrency());
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
        return lines.stream().filter(l -> l.getProductId().equals(productId)).findFirst();
    }

    private OrderLine line(UUID productId) {
        return findLine(productId).orElseThrow(
                () -> new DomainException("Product " + productId + " is not on this order"));
    }

    private List<OrderPlaced.Line> snapshotLines() {
        return lines.stream()
                .map(l -> new OrderPlaced.Line(l.getProductId(), l.getQuantity()))
                .collect(Collectors.toList());
    }

    // ---- read accessors --------------------------------------------------

    public OrderId getId() { return new OrderId(id); }
    public CustomerEmail getCustomerEmail() { return new CustomerEmail(customerEmail); }
    public OrderStatus getStatus() { return status; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPlacedAt() { return placedAt; }
}
