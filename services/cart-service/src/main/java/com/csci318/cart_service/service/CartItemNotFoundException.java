package com.csci318.cart_service.service;

public class CartItemNotFoundException extends RuntimeException {

	public CartItemNotFoundException(Long bookId) {
		super("Book " + bookId + " is not in the cart");
	}
}
