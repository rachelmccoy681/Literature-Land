package com.csci318.order_service.messaging;

import com.csci318.order_service.domain.StockAvailability;
import com.csci318.order_service.repository.StockAvailabilityRepository;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockEventConsumer {

    private final StockAvailabilityRepository stockAvailabilityRepository;

    public StockEventConsumer(StockAvailabilityRepository stockAvailabilityRepository) {
        this.stockAvailabilityRepository = stockAvailabilityRepository;
    }

    // receives stock updates from Kafka
    @Bean
    public Consumer<Map<String, Object>> stockUpdates() {
        return event -> {

            Long bookId = ((Number) event.get("bookId")).longValue();
            int stockQuantity = ((Number) event.get("stockQuantity")).intValue();

            // finds existing stock information or creates a new record
            StockAvailability stockAvailability = stockAvailabilityRepository
                    .findById(bookId)
                    .orElse(new StockAvailability(bookId, stockQuantity));

            // updates the stock quantity
            stockAvailability.setStockQuantity(stockQuantity);

            // saves the latest stock information
            stockAvailabilityRepository.save(stockAvailability);

            System.out.println(
                    "stock updated: book " + bookId +
                    " now has " + stockQuantity + " copies"
            );
        };
    }
}