package com.csci318.stock_service.request;

// stores the book and quantity requested
public class StockRequest {

    private Long bookId;
    private int quantity;

    public StockRequest() {
    }

    public StockRequest(Long bookId, int quantity) {
        this.bookId = bookId;
        this.quantity = quantity;
    }

    public Long getBookId() {
        return bookId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}