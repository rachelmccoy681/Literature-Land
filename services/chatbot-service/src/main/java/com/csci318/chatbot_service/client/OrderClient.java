package com.csci318.chatbot_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OrderClient {

    private final RestClient restClient;

    // connects to order service
    public OrderClient(@Value("${chatbot.services.order-url}") String orderUrl) {
        this.restClient = RestClient.create(orderUrl);
    }

    // gets the most searched and viewed books from the last 24 hours
    public List<TrendingBook> getTrendingBooks() {
        return restClient.get()
                .uri("/trending-books")
                .retrieve()
                .body(new ParameterizedTypeReference<List<TrendingBook>>() {});
    }

    // gets a customer's past orders
    public List<Order> getOrderHistory(long customerId) {
        return restClient.get()
                .uri("/orders/customer/{customerId}", customerId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Order>>() {});
    }

    // gets the books in one order
    public List<OrderItem> getOrderItems(long orderId) {
        return restClient.get()
                .uri("/orders/{orderId}/items", orderId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderItem>>() {});
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrendingBook(Long bookId, String title, String author, Integer activityCount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Order(Long id, Long customerId, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderItem(Long id, Long orderId, Long bookId, int quantity) {
    }
}
