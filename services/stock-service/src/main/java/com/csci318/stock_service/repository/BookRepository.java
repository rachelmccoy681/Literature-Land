package com.csci318.stock_service.repository;

import com.csci318.stock_service.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

// handles database operations for books
public interface BookRepository extends JpaRepository<Book, Long> {

}