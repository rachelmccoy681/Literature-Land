package com.csci318.cart_service.api;

import java.util.List;

public record CartResponse(Long customerId, List<CartItemResponse> items, long totalQuantity) {
	public CartResponse {
		items = List.copyOf(items);
	}
}
