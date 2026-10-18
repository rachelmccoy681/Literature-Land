package com.csci318.stock_service.controller;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.service.StockService;
import com.csci318.stock_service.request.StockRequest;

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

    // connects controller to stock service
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // allows customers to view all books
    @GetMapping
    public List<Book> getAllBooks() {
        return stockService.getAllBooks();
    }

    // allows customers to search books by keyword
    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String keyword) {
        return stockService.searchBooks(keyword);
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

    // allows customers to view a book using its ID
    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return stockService.getBookById(id);
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
}