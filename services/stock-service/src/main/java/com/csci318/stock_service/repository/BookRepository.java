package com.csci318.stock_service.repository;

import com.csci318.stock_service.domain.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

// handles database operations for books
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // searches books by title or author
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author);
}