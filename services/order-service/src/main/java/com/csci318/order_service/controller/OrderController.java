package com.csci318.order_service.controller;

import com.csci318.order_service.domain.Order;
import com.csci318.order_service.domain.OrderItem;
import com.csci318.order_service.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    // connects controller to order service
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // allows customers to place an order
    @PostMapping
    public Order placeOrder(
            @RequestParam Long customerId,
            @RequestBody List<OrderItem> items) {

        return orderService.placeOrder(customerId, items);
    }

    // allows customers to view their order history
    @GetMapping("/customer/{customerId}")
    public List<Order> getOrderHistory(@PathVariable Long customerId) {
        return orderService.getOrderHistory(customerId);
    }

    // allows customers to view books in an order
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }
}