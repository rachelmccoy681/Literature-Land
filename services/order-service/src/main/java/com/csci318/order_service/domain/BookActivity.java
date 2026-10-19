package com.csci318.order_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BookActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private long timestamp;

    public BookActivity() {
    }

    public BookActivity(Long bookId, long timestamp) {
        this.bookId = bookId;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public Long getBookId() {
        return bookId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}