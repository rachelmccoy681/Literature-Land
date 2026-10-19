package com.csci318.order_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class StockAvailability {

    @Id
    private Long bookId;

    private int stockQuantity;

    public StockAvailability() {
    }

    public StockAvailability(Long bookId, int stockQuantity) {
        this.bookId = bookId;
        this.stockQuantity = stockQuantity;
    }

    public Long getBookId() {
        return bookId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isAvailable() {
        return stockQuantity > 0;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}