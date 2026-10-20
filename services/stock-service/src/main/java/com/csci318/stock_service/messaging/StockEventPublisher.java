package com.csci318.stock_service.messaging;

import com.csci318.stock_service.event.StockUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class StockEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);

    private final StreamBridge streamBridge;
    private final boolean kafkaEnabled;

    public StockEventPublisher(
            StreamBridge streamBridge,
            @Value("${stock.kafka.enabled}") boolean kafkaEnabled) {
        this.streamBridge = streamBridge;
        this.kafkaEnabled = kafkaEnabled;
    }

    // sends updated stock information to Kafka
    public void publishStockUpdated(Long bookId, int stockQuantity) {

        // without kafka the order service's copy of the stock levels stops updating
        if (!kafkaEnabled) {
            return;
        }

        StockUpdatedEvent event = new StockUpdatedEvent(
                bookId,
                stockQuantity
        );

        // the stock change is already saved, so a failed send is logged instead of failing the request
        try {
            streamBridge.send("stockUpdates-out-0", event);
        } catch (RuntimeException e) {
            log.warn("could not publish stock update for book {}: {}", bookId, e.getMessage());
        }
    }
}
