package com.csci318.order_service.service;

import com.csci318.order_service.domain.BookActivity;
import com.csci318.order_service.repository.BookActivityRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrendingBooksService {

    private static final long TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000L;

    private final BookActivityRepository bookActivityRepository;

    public TrendingBooksService(BookActivityRepository bookActivityRepository) {
        this.bookActivityRepository = bookActivityRepository;
    }

    // save search and view activity in the database
    @Transactional
    public void recordActivity(Long bookId, long timestamp) {

        BookActivity activity = new BookActivity(bookId, timestamp);

        bookActivityRepository.save(activity);
    }

    // count activity for a specific book within the last 24 hours
    public int getActivityCount(Long bookId) {

        long cutoff = System.currentTimeMillis() - TWENTY_FOUR_HOURS;

        List<BookActivity> activities =
                bookActivityRepository.findByTimestampGreaterThanEqual(cutoff);

        return (int) activities.stream()
                .filter(activity -> activity.getBookId().equals(bookId))
                .count();
    }

    // retrieve trending books ranked by recent activity
    public List<Map<String, Object>> getTrendingBooks() {

        long cutoff = System.currentTimeMillis() - TWENTY_FOUR_HOURS;

        List<BookActivity> activities =
                bookActivityRepository.findByTimestampGreaterThanEqual(cutoff);

        Map<Long, Integer> activityCounts = new HashMap<>();

        // count activity for each book
        for (BookActivity activity : activities) {

            activityCounts.merge(
                    activity.getBookId(),
                    1,
                    Integer::sum
            );
        }

        List<Map<String, Object>> trendingBooks = new ArrayList<>();

        // prepare trending results
        activityCounts.forEach((bookId, count) -> {

            Map<String, Object> book = new HashMap<>();

            book.put("bookId", bookId);
            book.put("activityCount", count);

            trendingBooks.add(book);
        });

        // sort books by highest activity count
        trendingBooks.sort(
                Comparator.comparingInt(
                        (Map<String, Object> book) ->
                                (Integer) book.get("activityCount")
                ).reversed()
        );

        return trendingBooks;
    }

    // delete activity older than 24 hours
    @Transactional
    public void removeOldActivity() {

        long cutoff = System.currentTimeMillis() - TWENTY_FOUR_HOURS;

        bookActivityRepository.deleteByTimestampLessThan(cutoff);
    }
}