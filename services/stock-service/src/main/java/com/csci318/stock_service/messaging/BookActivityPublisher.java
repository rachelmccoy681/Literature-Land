package com.csci318.stock_service.messaging;

import com.csci318.stock_service.event.BookActivityEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class BookActivityPublisher {

    private final StreamBridge streamBridge;

    public BookActivityPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    // sends book search and view activity to Kafka
    public void publishBookActivity(Long bookId, String activityType) {

        BookActivityEvent event = new BookActivityEvent(
                bookId,
                activityType,
                System.currentTimeMillis()
        );

        streamBridge.send("bookActivity-out-0", event);
    }
}