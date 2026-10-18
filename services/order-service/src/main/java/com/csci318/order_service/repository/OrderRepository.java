package com.csci318.order_service.repository;

import com.csci318.order_service.domain.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// handles database operations for orders
public interface OrderRepository extends JpaRepository<Order, Long> {

    // finds all orders placed by a customer
    List<Order> findByCustomerId(Long customerId);
}