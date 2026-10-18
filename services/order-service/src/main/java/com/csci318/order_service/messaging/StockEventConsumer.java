package com.csci318.order_service.messaging;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockEventConsumer {

    // receives stock updates from kafka
    @Bean
    public Consumer<Map<String, Object>> stockUpdates() {
        return event -> {
            Long bookId = ((Number) event.get("bookId")).longValue();
            int stockQuantity = ((Number) event.get("stockQuantity")).intValue();

            System.out.println(
                    "stock updated: book " + bookId +
                    " now has " + stockQuantity + " copies"
            );
        };
    }
}