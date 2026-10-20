package com.csci318.stock_service.messaging;

import com.csci318.stock_service.event.BookActivityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class BookActivityPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookActivityPublisher.class);

    private final StreamBridge streamBridge;
    private final boolean kafkaEnabled;

    public BookActivityPublisher(
            StreamBridge streamBridge,
            @Value("${stock.kafka.enabled}") boolean kafkaEnabled) {
        this.streamBridge = streamBridge;
        this.kafkaEnabled = kafkaEnabled;
    }

    // sends book search and view activity to Kafka
    public void publishBookActivity(Long bookId, String activityType) {

        // without kafka the bookstore still works, only trending books stop updating
        if (!kafkaEnabled) {
            return;
        }

        BookActivityEvent event = new BookActivityEvent(
                bookId,
                activityType,
                System.currentTimeMillis()
        );

        // a lost activity event should not stop the customer from seeing the book
        try {
            streamBridge.send("bookActivity-out-0", event);
        } catch (RuntimeException e) {
            log.warn("could not publish book activity for book {}: {}", bookId, e.getMessage());
        }
    }
}
