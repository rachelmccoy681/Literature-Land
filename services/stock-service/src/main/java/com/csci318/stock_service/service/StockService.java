package com.csci318.stock_service.service;

import com.csci318.stock_service.domain.Book;
import com.csci318.stock_service.repository.BookRepository;
import com.csci318.stock_service.request.StockRequest;
import com.csci318.stock_service.messaging.StockEventPublisher;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StockService {

    private final BookRepository bookRepository;
    private final StockEventPublisher stockEventPublisher;

    // connects stock service to book repository and Kafka publisher
    public StockService(
            BookRepository bookRepository,
            StockEventPublisher stockEventPublisher) {

        this.bookRepository = bookRepository;
        this.stockEventPublisher = stockEventPublisher;
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
        Book savedBook = bookRepository.save(book);

        // sends Kafka event only after the transaction succeeds
        publishStockUpdateAfterCommit(
                savedBook.getId(),
                savedBook.getStockQuantity()
        );

        return savedBook;
    }

    // reduces stock for all books in an order
    @Transactional
    public void reduceStockForOrder(List<StockRequest> items) {

        // checks that the order contains books
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "order must contain at least one book"
            );
        }

        // combines quantities if the same book appears twice
        Map<Long, Integer> quantities = new TreeMap<>();

        for (StockRequest item : items) {

            // checks book ID and quantity
            if (item == null || item.getBookId() == null ||
                    item.getBookId() <= 0 || item.getQuantity() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "invalid book ID or quantity"
                );
            }

            try {
                quantities.merge(
                        item.getBookId(),
                        item.getQuantity(),
                        Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "quantity is too large"
                );
            }
        }

        // locks books in ID order to reduce deadlock risk
        Map<Long, Book> books = new TreeMap<>();

        for (Long bookId : quantities.keySet()) {

            Book book = bookRepository.findByIdForUpdate(bookId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "book not found: " + bookId
                    ));

            books.put(bookId, book);
        }

        // checks stock before reducing anything
        for (Long bookId : quantities.keySet()) {

            Book book = books.get(bookId);
            int quantity = quantities.get(bookId);

            if (book.getStockQuantity() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "not enough stock for book " + bookId
                );
            }
        }

        // reduces stock for all books
        for (Long bookId : quantities.keySet()) {

            Book book = books.get(bookId);
            int quantity = quantities.get(bookId);

            book.setStockQuantity(
                    book.getStockQuantity() - quantity
            );

            bookRepository.save(book);

            // sends Kafka event after the entire order transaction succeeds
            publishStockUpdateAfterCommit(
                    book.getId(),
                    book.getStockQuantity()
            );
        }
    }

    // sends stock updates to Kafka after the database transaction succeeds
    private void publishStockUpdateAfterCommit(Long bookId, int stockQuantity) {

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        stockEventPublisher.publishStockUpdated(
                                bookId,
                                stockQuantity
                        );
                    }
                }
        );
    }
}