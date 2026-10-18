package com.csci318.order_service.repository;

import com.csci318.order_service.domain.BookActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookActivityRepository extends JpaRepository<BookActivity, Long> {

    // find activity recorded within the last 24 hours
    List<BookActivity> findByTimestampGreaterThanEqual(long timestamp);

    // remove activity older than 24 hours
    void deleteByTimestampLessThan(long timestamp);
}