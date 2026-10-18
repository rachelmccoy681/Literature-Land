package com.csci318.cart_service.service;

public class CartNotFoundException extends RuntimeException {

	public CartNotFoundException(Long customerId) {
		super("Cart not found for customer " + customerId);
	}
}
