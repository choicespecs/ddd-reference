package com.example.shop.ordering.domain;

import com.example.shop.ordering.domain.events.OrderCancelled;
import com.example.shop.ordering.domain.events.OrderPlaced;
import com.example.shop.sharedkernel.DomainException;
import com.example.shop.sharedkernel.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests: no Spring, no database — milliseconds to run. This is where most of your
 * tests should live in a DDD codebase, because this is where the business rules live.
 */
class OrderTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    private final UUID book = UUID.randomUUID();
    private final UUID mug = UUID.randomUUID();

    private Order draft() {
        return Order.startDraft(new CustomerEmail("Ada@Example.com"), clock);
    }

    @Test
    void new_order_is_draft_with_normalized_email() {
        Order order = draft();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.getCustomerEmail().getValue()).isEqualTo("ada@example.com");
        assertThat(order.total()).isEqualTo(Money.of("0", "USD"));
    }

    @Test
    void adding_same_product_twice_merges_quantity() {
        Order order = draft();
        order.addLine(book, "DDD", Money.of("50.00", "USD"), 1);
        order.addLine(book, "DDD", Money.of("50.00", "USD"), 2);

        assertThat(order.getLines()).hasSize(1);
        assertThat(order.getLines().get(0).getQuantity()).isEqualTo(3);
        assertThat(order.total()).isEqualTo(Money.of("150.00", "USD"));
    }

    @Test
    void total_sums_all_lines() {
        Order order = draft();
        order.addLine(book, "DDD", Money.of("54.99", "USD"), 2);
        order.addLine(mug, "Mug", Money.of("14.00", "USD"), 1);
        assertThat(order.total()).isEqualTo(Money.of("123.98", "USD"));
    }

    @Test
    void lines_must_share_a_currency() {
        Order order = draft();
        order.addLine(book, "DDD", Money.of("50.00", "USD"), 1);
        assertThatThrownBy(() -> order.addLine(mug, "Mug", Money.of("10.00", "EUR"), 1))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("USD");
    }

    @Test
    void quantity_is_bounded() {
        Order order = draft();
        assertThatThrownBy(() -> order.addLine(book, "DDD", Money.of("1.00", "USD"), 0))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> order.addLine(book, "DDD", Money.of("1.00", "USD"), 100))
                .isInstanceOf(DomainException.class);
    }

    @Nested
    class Placing {

        @Test
        void empty_order_cannot_be_placed() {
            assertThatThrownBy(() -> draft().place(clock))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        void placing_changes_status_and_raises_event() {
            Order order = draft();
            order.addLine(book, "DDD", Money.of("50.00", "USD"), 2);

            order.place(clock);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
            assertThat(order.getPlacedAt()).isEqualTo(clock.instant());
            assertThat(order.peekDomainEvents())
                    .singleElement()
                    .isInstanceOfSatisfying(OrderPlaced.class, e -> {
                        assertThat(e.getTotal()).isEqualTo(Money.of("100.00", "USD"));
                        assertThat(e.getLines()).containsExactly(new OrderPlaced.Line(book, 2));
                    });
        }

        @Test
        void placed_order_is_frozen() {
            Order order = draft();
            order.addLine(book, "DDD", Money.of("50.00", "USD"), 1);
            order.place(clock);

            assertThatThrownBy(() -> order.addLine(mug, "Mug", Money.of("1.00", "USD"), 1))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("PLACED");
            assertThatThrownBy(() -> order.removeLine(book)).isInstanceOf(DomainException.class);
        }
    }

    @Nested
    class Cancelling {

        @Test
        void cancelling_a_draft_raises_no_event() {
            Order order = draft();
            order.cancel(clock);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.peekDomainEvents()).isEmpty();
        }

        @Test
        void cancelling_a_placed_order_raises_event_to_release_stock() {
            Order order = draft();
            order.addLine(book, "DDD", Money.of("50.00", "USD"), 1);
            order.place(clock);
            order.pullDomainEvents();

            order.cancel(clock);

            assertThat(order.peekDomainEvents()).singleElement().isInstanceOf(OrderCancelled.class);
        }

        @Test
        void shipped_order_cannot_be_cancelled() {
            Order order = draft();
            order.addLine(book, "DDD", Money.of("50.00", "USD"), 1);
            order.place(clock);
            order.ship();
            assertThatThrownBy(() -> order.cancel(clock)).isInstanceOf(DomainException.class);
        }
    }
}
