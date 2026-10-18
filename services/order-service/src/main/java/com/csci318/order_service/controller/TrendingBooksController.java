package com.csci318.order_service.controller;

import com.csci318.order_service.service.TrendingBooksService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trending-books")
public class TrendingBooksController {

    private final TrendingBooksService trendingBooksService;
    private final RestTemplate restTemplate;

    public TrendingBooksController(TrendingBooksService trendingBooksService) {
        this.trendingBooksService = trendingBooksService;
        this.restTemplate = new RestTemplate();
    }

    // returns trending books with their details
    @GetMapping
    public List<Map<String, Object>> getTrendingBooks() {

        List<Map<String, Object>> trendingBooks =
                trendingBooksService.getTrendingBooks();

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> trendingBook : trendingBooks) {

            Long bookId = ((Number) trendingBook.get("bookId")).longValue();

            try {

                // retrieves book details without recording another view event
                @SuppressWarnings("unchecked")
                Map<String, Object> bookDetails = restTemplate.getForObject(
                        "http://localhost:8081/books/" + bookId + "/details",
                        Map.class
                );

                if (bookDetails != null) {

                    Map<String, Object> result = new LinkedHashMap<>();

                    result.put("bookId", bookId);
                    result.put("title", bookDetails.get("title"));
                    result.put("author", bookDetails.get("author"));
                    result.put("activityCount", trendingBook.get("activityCount"));

                    results.add(result);
                }

            } catch (Exception e) {
                System.err.println("Could not retrieve details for book " + bookId);
            }
        }

        return results;
    }
}