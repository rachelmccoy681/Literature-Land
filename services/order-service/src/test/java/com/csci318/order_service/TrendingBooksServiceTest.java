package com.csci318.order_service;

import com.csci318.order_service.domain.BookActivity;
import com.csci318.order_service.repository.BookActivityRepository;
import com.csci318.order_service.service.TrendingBooksService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TrendingBooksServiceTest {

    @Mock
    private BookActivityRepository bookActivityRepository;

    private TrendingBooksService trendingBooksService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        trendingBooksService =
                new TrendingBooksService(bookActivityRepository);
    }

    @Test
    void shouldCountOnlyActivityFromLast24Hours() {

        long now = System.currentTimeMillis();

        // recent activity
        BookActivity recentSearch = new BookActivity(1L, now);
        BookActivity recentView = new BookActivity(1L, now);

        // activity older than 24 hours
        BookActivity oldView = new BookActivity(
                1L,
                now - (25L * 60 * 60 * 1000)
        );

        // repository returns only recent activity
        when(bookActivityRepository.findByTimestampGreaterThanEqual(anyLong()))
                .thenAnswer(invocation -> {
                    long cutoff = invocation.getArgument(0);

                    return List.of(recentSearch, recentView, oldView)
                            .stream()
                            .filter(activity -> activity.getTimestamp() >= cutoff)
                            .toList();
                });

        int count = trendingBooksService.getActivityCount(1L);

        assertEquals(2, count);
    }

    @Test
    void shouldRankBooksByActivityCount() {

        long now = System.currentTimeMillis();

        when(bookActivityRepository.findByTimestampGreaterThanEqual(anyLong()))
                .thenReturn(List.of(
                        new BookActivity(1L, now),
                        new BookActivity(1L, now),
                        new BookActivity(2L, now)
                ));

        List<Map<String, Object>> trendingBooks =
                trendingBooksService.getTrendingBooks();

        assertEquals(2, trendingBooks.size());

        assertEquals(1L, trendingBooks.get(0).get("bookId"));
        assertEquals(2, trendingBooks.get(0).get("activityCount"));

        assertEquals(2L, trendingBooks.get(1).get("bookId"));
        assertEquals(1, trendingBooks.get(1).get("activityCount"));
    }
}