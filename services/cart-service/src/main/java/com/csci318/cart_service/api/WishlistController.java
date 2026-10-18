package com.csci318.cart_service.api;

import com.csci318.cart_service.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistController {

	private final WishlistService wishlistService;

	public WishlistController(WishlistService wishlistService) {
		this.wishlistService = wishlistService;
	}

	@GetMapping("/{customerId}")
	public WishlistResponse getWishlist(@PathVariable Long customerId) {
		return wishlistService.getWishlist(customerId);
	}

	@PostMapping("/{customerId}/items")
	public WishlistResponse addItem(
			@PathVariable Long customerId,
			@Valid @RequestBody AddWishlistItemRequest request) {
		return wishlistService.addItem(customerId, request.bookId());
	}

	@DeleteMapping("/{customerId}/items/{bookId}")
	public ResponseEntity<Void> removeItem(
			@PathVariable Long customerId,
			@PathVariable Long bookId) {
		wishlistService.removeItem(customerId, bookId);
		return ResponseEntity.noContent().build();
	}
}
