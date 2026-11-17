package com.example.shop.ordering.application;

import com.example.shop.ordering.domain.CustomerEmail;
import com.example.shop.ordering.domain.Order;
import com.example.shop.ordering.domain.OrderId;
import com.example.shop.ordering.domain.OrderRepository;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use cases of the Ordering context. Each public method is one transaction and follows the
 * same shape:
 * <pre>
 *   1. load the aggregate (or create it)
 *   2. call ONE behaviour method on it
 *   3. save it
 *   4. publish the domain events it raised
 *   5. return a read model
 * </pre>
 */
@Service
@Transactional
public class OrderApplicationService {

    private final OrderRepository orders;
    private final ProductCatalog catalog;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public OrderApplicationService(OrderRepository orders, ProductCatalog catalog,
                                   ApplicationEventPublisher events, Clock clock) {
        this.orders = orders;
        this.catalog = catalog;
        this.events = events;
        this.clock = clock;
    }

    public OrderView startOrder(Commands.StartOrder cmd) {
        Order order = Order.startDraft(new CustomerEmail(cmd.getCustomerEmail()), clock);
        return saveAndPublish(order);
    }

    public OrderView addLine(Commands.AddLine cmd) {
        Order order = load(cmd.getOrderId());
        ProductCatalog.OrderableProduct product = catalog.find(cmd.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", cmd.getProductId()));
        if (!product.isAvailable()) {
            throw new DomainException("Product " + product.getName() + " is not available");
        }
        order.addLine(product.getProductId(), product.getName(), product.getPrice(), cmd.getQuantity());
        return saveAndPublish(order);
    }

    public OrderView changeQuantity(Commands.ChangeQuantity cmd) {
        Order order = load(cmd.getOrderId());
        order.changeQuantity(cmd.getProductId(), cmd.getQuantity());
        return saveAndPublish(order);
    }

    public OrderView removeLine(Commands.RemoveLine cmd) {
        Order order = load(cmd.getOrderId());
        order.removeLine(cmd.getProductId());
        return saveAndPublish(order);
    }

    public OrderView place(UUID orderId) {
        Order order = load(orderId);
        order.place(clock);
        return saveAndPublish(order);
    }

    public OrderView ship(UUID orderId) {
        Order order = load(orderId);
        order.ship();
        return saveAndPublish(order);
    }

    public OrderView cancel(UUID orderId) {
        Order order = load(orderId);
        order.cancel(clock);
        return saveAndPublish(order);
    }

    @Transactional(readOnly = true)
    public OrderView get(UUID orderId) {
        return OrderView.from(load(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderView> recent(int limit) {
        return orders.findRecent(limit).stream().map(OrderView::from).collect(Collectors.toList());
    }

    private Order load(UUID orderId) {
        return orders.findById(new OrderId(orderId))
                .orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    private OrderView saveAndPublish(Order order) {
        Order saved = orders.save(order);
        // Events are published inside the transaction; synchronous listeners join it.
        order.pullDomainEvents().forEach(events::publishEvent);
        return OrderView.from(saved);
    }
}
