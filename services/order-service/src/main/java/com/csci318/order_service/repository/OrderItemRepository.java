package com.csci318.order_service.repository;

import com.csci318.order_service.domain.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// handles database operations for order items
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // finds all books belonging to an order
    List<OrderItem> findByOrderId(Long orderId);
}