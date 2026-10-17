package com.csci318.cart_service.repository;

import com.csci318.cart_service.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
}
