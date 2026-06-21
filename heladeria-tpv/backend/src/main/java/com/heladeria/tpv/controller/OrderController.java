package com.heladeria.tpv.controller;

import com.heladeria.tpv.dto.CreateOrderRequest;
import com.heladeria.tpv.dto.OrderResponse;
import com.heladeria.tpv.model.Order;
import com.heladeria.tpv.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new OrderResponse(order);
    }

    @GetMapping("/{id}")
    public OrderResponse findById(@PathVariable Long id) {
        return new OrderResponse(orderService.findById(id));
    }

    @PostMapping("/{id}/void")
    public OrderResponse voidOrder(@PathVariable Long id, @RequestParam Long voidedByUserId) {
        Order order = orderService.voidOrder(id, voidedByUserId);
        return new OrderResponse(order);
    }
}
