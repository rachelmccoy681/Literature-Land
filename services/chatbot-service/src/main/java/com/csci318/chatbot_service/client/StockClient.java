package com.csci318.chatbot_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class StockClient {

    private final RestClient restClient;

    // connects to stock service
    public StockClient(@Value("${chatbot.services.stock-url}") String stockUrl) {
        this.restClient = RestClient.create(stockUrl);
    }

    // searches books by title, author or genre (stock service records this as search activity for trending books)
    public List<Book> searchBooks(String keyword) {
        return restClient.get()
                .uri(uri -> uri.path("/books/search")
                        .queryParam("keyword", keyword)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>() {});
    }

    // filters books, leaving out any filter that is null
    public List<Book> filterBooks(
            String genre,
            String author,
            Double minPrice,
            Double maxPrice,
            Boolean available,
            Double minRating) {

        return restClient.get()
                .uri(uri -> uri.path("/books/filter")
                        .queryParamIfPresent("genre", Optional.ofNullable(genre))
                        .queryParamIfPresent("author", Optional.ofNullable(author))
                        .queryParamIfPresent("minPrice", Optional.ofNullable(minPrice))
                        .queryParamIfPresent("maxPrice", Optional.ofNullable(maxPrice))
                        .queryParamIfPresent("available", Optional.ofNullable(available))
                        .queryParamIfPresent("minRating", Optional.ofNullable(minRating))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Book>>() {});
    }

    // gets book details without recording a view event
    public Book getBookDetails(long bookId) {
        return restClient.get()
                .uri("/books/{id}/details", bookId)
                .retrieve()
                .body(Book.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Book(
            Long id,
            String title,
            String author,
            String genre,
            Double price,
            Double rating,
            Integer stockQuantity,
            Boolean available,
            String description
    ) {
    }
}
