package com.csci318.cart_service.service;

public class WishlistItemNotFoundException extends RuntimeException {

	public WishlistItemNotFoundException(Long bookId) {
		super("Book " + bookId + " is not in the wishlist");
	}
}
