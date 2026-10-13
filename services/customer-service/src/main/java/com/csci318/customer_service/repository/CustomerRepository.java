package com.csci318.customer_service.repository;

import com.csci318.customer_service.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

// handles customer data in the database
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}