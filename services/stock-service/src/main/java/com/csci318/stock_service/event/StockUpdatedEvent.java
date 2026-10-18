package com.csci318.stock_service.event;

public record StockUpdatedEvent(
        Long bookId,
        int stockQuantity
) {
}