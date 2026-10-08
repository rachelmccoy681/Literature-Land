package com.csci318.stock_service.service;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockService {

    private final BookRepository bookRepository;

    // connects stock service to book repository
    public StockService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // gets all books stored in the database
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
}