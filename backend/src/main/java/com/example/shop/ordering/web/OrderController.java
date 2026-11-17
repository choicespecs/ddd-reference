package com.example.shop.ordering.web;

import com.example.shop.ordering.application.Commands;
import com.example.shop.ordering.application.OrderApplicationService;
import com.example.shop.ordering.application.OrderView;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST adapter for Ordering. State transitions are modelled as sub-resource POSTs
 * ({@code /place}, {@code /cancel}) rather than "PATCH status=PLACED", so the API speaks the
 * ubiquitous language and the domain stays in charge of what transitions are legal.
 */
@RestController
@RequestMapping("/api/orders")
class OrderController {

    private final OrderApplicationService orders;

    OrderController(OrderApplicationService orders) {
        this.orders = orders;
    }

    @GetMapping
    List<OrderView> recent(@RequestParam(defaultValue = "20") int limit) {
        return orders.recent(Math.min(limit, 100));
    }

    @GetMapping("/{id}")
    OrderView get(@PathVariable UUID id) {
        return orders.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OrderView start(@Valid @RequestBody StartOrderRequest req) {
        return orders.startOrder(new Commands.StartOrder(req.getCustomerEmail()));
    }

    @PostMapping("/{id}/lines")
    OrderView addLine(@PathVariable UUID id, @Valid @RequestBody AddLineRequest req) {
        return orders.addLine(new Commands.AddLine(id, req.getProductId(), req.getQuantity()));
    }

    @PutMapping("/{id}/lines/{productId}")
    OrderView changeQuantity(@PathVariable UUID id, @PathVariable UUID productId,
                             @Valid @RequestBody ChangeQuantityRequest req) {
        return orders.changeQuantity(new Commands.ChangeQuantity(id, productId, req.getQuantity()));
    }

    @DeleteMapping("/{id}/lines/{productId}")
    OrderView removeLine(@PathVariable UUID id, @PathVariable UUID productId) {
        return orders.removeLine(new Commands.RemoveLine(id, productId));
    }

    @PostMapping("/{id}/place")
    OrderView place(@PathVariable UUID id) {
        return orders.place(id);
    }

    @PostMapping("/{id}/ship")
    OrderView ship(@PathVariable UUID id) {
        return orders.ship(id);
    }

    @PostMapping("/{id}/cancel")
    OrderView cancel(@PathVariable UUID id) {
        return orders.cancel(id);
    }

    @Value
    static class StartOrderRequest {
        @NotBlank @Email String customerEmail;
    }

    @Value
    static class AddLineRequest {
        @NotNull UUID productId;
        @Min(1) @Max(99) int quantity;
    }

    @Value
    static class ChangeQuantityRequest {
        @Min(1) @Max(99) int quantity;
    }
}
