package com.csci318.stock_service.service;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.repository.BookRepository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

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

    // searches books by title or author
    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                keyword, keyword);
    }

    // filters books based on customer preferences
    public List<Book> filterBooks(
            String genre,
            String author,
            Double minPrice,
            Double maxPrice,
            Boolean available,
            Double minRating) {

        Specification<Book> specification = (root, query, cb) -> cb.conjunction();

        // filters by genre
        if (genre != null && !genre.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("genre")), genre.toLowerCase()));
        }

        // filters by author
        if (author != null && !author.isBlank()) {
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("author")),
                            "%" + author.toLowerCase() + "%"));
        }

        // filters by minimum price
        if (minPrice != null) {
            specification = specification.and((root, query, cb) ->
                    cb.ge(root.get("price"), minPrice));
        }

        // filters by maximum price
        if (maxPrice != null) {
            specification = specification.and((root, query, cb) ->
                    cb.le(root.get("price"), maxPrice));
        }

        // filters by availability
        if (available != null) {
            specification = specification.and((root, query, cb) ->
                    available
                            ? cb.gt(root.get("stockQuantity"), 0)
                            : cb.le(root.get("stockQuantity"), 0));
        }

        // filters by minimum rating
        if (minRating != null) {
            specification = specification.and((root, query, cb) ->
                    cb.ge(root.get("rating"), minRating));
        }

        return bookRepository.findAll(specification);
    }

    // gets a book using its ID
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found"
                ));
    }

    // reduces stock when books are purchased
    @Transactional
    public Book reduceStock(Long id, int quantity) {

        // checks that the quantity is valid
        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity must be greater than zero"
            );
        }

        // finds and locks the book
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found"
                ));

        // checks if enough stock is available
        if (book.getStockQuantity() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "not enough stock available"
            );
        }

        // updates the stock quantity
        book.setStockQuantity(book.getStockQuantity() - quantity);

        // saves the updated stock
        return bookRepository.save(book);
    }
}