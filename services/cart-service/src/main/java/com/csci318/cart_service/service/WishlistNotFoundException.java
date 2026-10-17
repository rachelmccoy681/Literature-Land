package com.csci318.cart_service.service;

public class WishlistNotFoundException extends RuntimeException {

	public WishlistNotFoundException(Long customerId) {
		super("Wishlist not found for customer " + customerId);
	}
}
