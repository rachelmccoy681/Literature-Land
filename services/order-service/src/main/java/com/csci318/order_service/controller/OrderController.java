package com.csci318.order_service.controller;

import com.csci318.order_service.domain.Order;
import com.csci318.order_service.domain.OrderItem;
import com.csci318.order_service.service.OrderService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ALLOW REQUESTS FROM THE LOCAL FRONTEND
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    // CONNECT CONTROLLER TO ORDER SERVICE
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ALLOW CUSTOMERS TO PLACE AN ORDER
    @PostMapping
    public Order placeOrder(
            @RequestParam Long customerId,
            @RequestBody List<OrderItem> items) {

        return orderService.placeOrder(customerId, items);
    }

    // ALLOW CUSTOMERS TO VIEW THEIR ORDER HISTORY
    @GetMapping("/customer/{customerId}")
    public List<Order> getOrderHistory(
            @PathVariable Long customerId) {

        return orderService.getOrderHistory(customerId);
    }

    // ALLOW CUSTOMERS TO VIEW BOOKS IN AN ORDER
    @GetMapping("/{orderId}/items")
    public List<OrderItem> getOrderItems(
            @PathVariable Long orderId) {

        return orderService.getOrderItems(orderId);
    }
}