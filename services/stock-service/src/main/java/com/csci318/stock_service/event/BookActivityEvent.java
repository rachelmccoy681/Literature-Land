package com.csci318.stock_service.event;

public record BookActivityEvent(
        Long bookId,
        String activityType,
        long timestamp
) {
}