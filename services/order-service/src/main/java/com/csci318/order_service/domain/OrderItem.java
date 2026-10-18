package com.csci318.order_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// stores the books included in an order
@Entity
public class OrderItem {

    // each order item has its own ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // order this item belongs to
    private Long orderId;

    // book being purchased
    private Long bookId;

    // number of copies purchased
    private int quantity;

    public OrderItem() {
    }

    // creates a new order item
    public OrderItem(Long orderId, Long bookId, int quantity) {
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getBookId() {
        return bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}