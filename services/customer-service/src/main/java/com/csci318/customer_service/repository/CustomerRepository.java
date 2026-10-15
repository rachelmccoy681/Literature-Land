package com.csci318.customer_service.repository;

import com.csci318.customer_service.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // checks if a customer already uses this email
    boolean existsByEmailIgnoreCase(String email);

    // checks if a username is already taken
    boolean existsByUsernameIgnoreCase(String username);

    // finds a customer using their email
    Optional<Customer> findByEmailIgnoreCase(String email);
}