package com.csci318.order_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// stores customer order information
@Entity
@Table(name = "customer_orders")
public class Order {

    // each order has its own ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // customer who placed the order
    private Long customerId;

    // current order status
    private String status;

    public Order() {
    }

    // creates a new order
    public Order(Long customerId) {
        this.customerId = customerId;
        this.status = "PENDING";
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}