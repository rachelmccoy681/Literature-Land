package com.csci318.order_service.messaging;

import com.csci318.order_service.service.TrendingBooksService;

import java.util.Map;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookActivityConsumer {

    private final TrendingBooksService trendingBooksService;

    public BookActivityConsumer(TrendingBooksService trendingBooksService) {
        this.trendingBooksService = trendingBooksService;
    }

    // receives book search and view events from kafka
    @Bean
    public Consumer<Map<String, Object>> bookActivity() {
        return event -> {

            Long bookId = ((Number) event.get("bookId")).longValue();
            String activityType = (String) event.get("activityType");
            long timestamp = ((Number) event.get("timestamp")).longValue();

            // counts valid search and view events
            if ("SEARCH".equals(activityType) || "VIEW".equals(activityType)) {
                trendingBooksService.recordActivity(bookId, timestamp);
            }

            System.out.println(
                    "book activity: " + activityType +
                    " for book " + bookId +
                    " at " + timestamp
            );
        };
    }
}