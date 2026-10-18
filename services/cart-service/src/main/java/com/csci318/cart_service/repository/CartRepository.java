package com.csci318.cart_service.repository;

import com.csci318.cart_service.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
