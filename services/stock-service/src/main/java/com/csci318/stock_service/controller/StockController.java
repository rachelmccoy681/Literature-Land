package com.csci318.stock_service.controller;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}