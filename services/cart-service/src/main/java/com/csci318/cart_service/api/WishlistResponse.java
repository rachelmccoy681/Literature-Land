package com.csci318.cart_service.api;

import java.util.List;

public record WishlistResponse(Long customerId, List<WishlistItemResponse> items) {
	public WishlistResponse {
		items = List.copyOf(items);
	}
}
