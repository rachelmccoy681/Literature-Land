package com.csci318.cart_service.api;

import com.csci318.cart_service.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping("/{customerId}")
	public CartResponse getCart(
			@PathVariable Long customerId,
			@RequestParam(defaultValue = "bookId") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		return cartService.getCart(customerId, sortBy, direction);
	}

	@PostMapping("/{customerId}/items")
	public CartResponse addItem(
			@PathVariable Long customerId,
			@Valid @RequestBody AddCartItemRequest request) {
		return cartService.addItem(customerId, request.bookId(), request.quantity());
	}

	@PutMapping("/{customerId}/items/{bookId}")
	public CartResponse updateItem(
			@PathVariable Long customerId,
			@PathVariable Long bookId,
			@Valid @RequestBody UpdateCartItemRequest request) {
		return cartService.updateItem(customerId, bookId, request.quantity());
	}

	@DeleteMapping("/{customerId}/items/{bookId}")
	public ResponseEntity<Void> removeItem(
			@PathVariable Long customerId,
			@PathVariable Long bookId) {
		cartService.removeItem(customerId, bookId);
		return ResponseEntity.noContent().build();
	}
}
