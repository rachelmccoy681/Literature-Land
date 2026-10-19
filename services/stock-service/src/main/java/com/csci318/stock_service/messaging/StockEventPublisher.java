package com.csci318.stock_service.messaging;

import com.csci318.stock_service.event.StockUpdatedEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class StockEventPublisher {

    private final StreamBridge streamBridge;

    public StockEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    // sends updated stock information to Kafka
    public void publishStockUpdated(Long bookId, int stockQuantity) {

        StockUpdatedEvent event = new StockUpdatedEvent(
                bookId,
                stockQuantity
        );

        streamBridge.send("stockUpdates-out-0", event);
    }
}