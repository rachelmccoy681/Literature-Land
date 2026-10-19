package com.csci318.stock_service.controller;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.service.StockService;
import com.csci318.stock_service.request.StockRequest;
import com.csci318.stock_service.messaging.BookActivityPublisher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/books")
public class StockController {

    private final StockService stockService;
    private final BookActivityPublisher bookActivityPublisher;

    // connects controller to stock service and kafka publisher
    public StockController(
            StockService stockService,
            BookActivityPublisher bookActivityPublisher) {
        this.stockService = stockService;
        this.bookActivityPublisher = bookActivityPublisher;
    }

    // allows customers to view all books
    @GetMapping
    public List<Book> getAllBooks() {
        return stockService.getAllBooks();
    }

    // searches books and publishes search activity to kafka
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

    // allows customers to filter books
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

    // retrieves book details and publishes view activity to kafka
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {

        Book book = stockService.getBookById(id);

        bookActivityPublisher.publishBookActivity(
                book.getId(),
                "VIEW");

        return book;
    }

    // reduces stock when books are purchased
    @PostMapping("/{id}/reduce-stock")
    public Book reduceStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        return stockService.reduceStock(id, quantity);
    }

    // reduces stock for multiple books in one order
    @PostMapping("/reduce-stock")
    public void reduceStockForOrder(@RequestBody List<StockRequest> items) {
        stockService.reduceStockForOrder(items);
    }

    // get book details without recording a view event
    @GetMapping("/{id}/details")
    public Book getBookDetails(@PathVariable Long id) {
        return stockService.getBookById(id);
    }
}