package com.csci318.order_service.service;

import com.csci318.order_service.client.StockClient;
import com.csci318.order_service.domain.Order;
import com.csci318.order_service.domain.OrderItem;
import com.csci318.order_service.repository.OrderRepository;
import com.csci318.order_service.repository.OrderItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockClient stockClient;

    // connects service to repositories and stock service
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        StockClient stockClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.stockClient = stockClient;
    }

    // creates an order and saves its books
    @Transactional
    public Order placeOrder(Long customerId, List<OrderItem> items) {

        // checks customer ID
        if (customerId == null || customerId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "invalid customer ID"
            );
        }

        // checks that the order contains books
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "order must contain at least one book"
            );
        }

        // checks book IDs and quantities
        for (OrderItem item : items) {
            if (item == null || item.getBookId() == null ||
                    item.getBookId() <= 0 || item.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "invalid book ID or quantity"
                );
            }
        }

        // reduces stock for all books in the order
        try {
            stockClient.reduceStockForOrder(items);
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "order could not be placed because a book is unavailable"
            );
        }

        // creates a new order
        Order order = new Order(customerId);
        order = orderRepository.save(order);

        // saves each book under the order
        for (OrderItem item : items) {
            OrderItem orderItem = new OrderItem(
                    order.getId(),
                    item.getBookId(),
                    item.getQuantity()
            );

            orderItemRepository.save(orderItem);
        }

        return order;
    }

    // gets all orders placed by a customer
    public List<Order> getOrderHistory(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // gets the books belonging to an order
    public List<OrderItem> getOrderItems(Long orderId) {

        // checks that the order exists
        if (!orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "order not found"
            );
        }

        return orderItemRepository.findByOrderId(orderId);
    }
}