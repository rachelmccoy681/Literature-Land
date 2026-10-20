package com.csci318.stock_service.controller;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.service.StockService;
import com.csci318.stock_service.request.StockRequest;
import com.csci318.stock_service.messaging.BookActivityPublisher;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ALLOW FRONTEND TO ACCESS STOCK SERVICE

@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
})
@RestController
@RequestMapping("/books")
public class StockController {

    private final StockService stockService;
    private final BookActivityPublisher bookActivityPublisher;

    // CONNECTS CONTROLLER TO STOCK SERVICE AND KAFKA PUBLISHER

    public StockController(
            StockService stockService,
            BookActivityPublisher bookActivityPublisher) {
        this.stockService = stockService;
        this.bookActivityPublisher = bookActivityPublisher;
    }

    // ALLOWS CUSTOMERS TO VIEW ALL BOOKS

    @GetMapping
    public List<Book> getAllBooks() {
        return stockService.getAllBooks();
    }

    // SEARCHES BOOKS AND PUBLISHES SEARCH ACTIVITY TO KAFKA

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String keyword) {

        List<Book> books = stockService.searchBooks(keyword);

        for (Book book : books) {
            bookActivityPublisher.publishBookActivity(
                    book.getId(),
                    "SEARCH");
        }

        return books;
    }

    // ALLOWS CUSTOMERS TO FILTER BOOKS

    @GetMapping("/filter")
    public List<Book> filterBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Double minRating) {

        return stockService.filterBooks(
                genre, author, minPrice, maxPrice, available, minRating);
    }

    // RETRIEVES BOOK DETAILS AND PUBLISHES VIEW ACTIVITY TO KAFKA

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {

        Book book = stockService.getBookById(id);

        bookActivityPublisher.publishBookActivity(
                book.getId(),
                "VIEW");

        return book;
    }

    // REDUCES STOCK WHEN BOOKS ARE PURCHASED

    @PostMapping("/{id}/reduce-stock")
    public Book reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        return stockService.reduceStock(id, quantity);
    }

    // REDUCES STOCK FOR MULTIPLE BOOKS IN ONE ORDER

    @PostMapping("/reduce-stock")
    public void reduceStockForOrder(@RequestBody List<StockRequest> items) {
        stockService.reduceStockForOrder(items);
    }

    // GET BOOK DETAILS WITHOUT RECORDING A VIEW EVENT

    @GetMapping("/{id}/details")
    public Book getBookDetails(@PathVariable Long id) {
        return stockService.getBookById(id);
    }
}