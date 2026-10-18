package com.csci318.order_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class StockClient {

    private final RestClient restClient;

    // connects to stock service
    public StockClient() {
        this.restClient = RestClient.create("http://localhost:8081");
    }

    // gets the available stock for a book
    public int getStockQuantity(Long bookId) {

        Map<?, ?> book = restClient.get()
                .uri("/books/{id}", bookId)
                .retrieve()
                .body(Map.class);

        if (book == null || book.get("stockQuantity") == null) {
            throw new IllegalStateException("could not get book stock");
        }

        return ((Number) book.get("stockQuantity")).intValue();
    }

    // reduces stock for all books in an order
    public void reduceStockForOrder(List<?> items) {

        restClient.post()
                .uri("/books/reduce-stock")
                .body(items)
                .retrieve()
                .toBodilessEntity();
    }
}