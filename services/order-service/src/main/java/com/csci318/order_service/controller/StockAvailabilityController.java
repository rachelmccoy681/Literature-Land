package com.csci318.order_service.controller;

import com.csci318.order_service.domain.StockAvailability;
import com.csci318.order_service.repository.StockAvailabilityRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/stock-availability")
public class StockAvailabilityController {

    private final StockAvailabilityRepository stockAvailabilityRepository;

    public StockAvailabilityController(
            StockAvailabilityRepository stockAvailabilityRepository) {
        this.stockAvailabilityRepository = stockAvailabilityRepository;
    }

    // gets the latest stock information received through Kafka
    @GetMapping("/{bookId}")
    public StockAvailability getStockAvailability(@PathVariable Long bookId) {

        return stockAvailabilityRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "stock availability not found for book " + bookId
                ));
    }
}