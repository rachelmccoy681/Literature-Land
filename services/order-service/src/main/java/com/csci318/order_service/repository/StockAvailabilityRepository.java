package com.csci318.order_service.repository;

import com.csci318.order_service.domain.StockAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAvailabilityRepository extends JpaRepository<StockAvailability, Long> {
}