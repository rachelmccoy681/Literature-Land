package com.csci318.cart_service.service;

import com.csci318.cart_service.api.CartItemResponse;
import com.csci318.cart_service.api.CartResponse;
import com.csci318.cart_service.domain.Cart;
import com.csci318.cart_service.domain.CartItem;
import com.csci318.cart_service.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CartService {

	private final CartRepository cartRepository;

	public CartService(CartRepository cartRepository) {
		this.cartRepository = cartRepository;
	}

	public CartResponse addItem(Long customerId, Long bookId, int quantity) {
		requirePositive(customerId, "customerId");
		requirePositive(bookId, "bookId");
		requirePositive(quantity, "quantity");
		Cart cart = cartRepository.findById(customerId)
				.orElseGet(() -> new Cart(customerId));

		CartItem item = cart.findItemByBookId(bookId)
				.orElseGet(() -> {
					CartItem newItem = new CartItem(bookId, 0);
					cart.addItem(newItem);
					return newItem;
				});
		item.increaseQuantity(quantity);

		return toResponse(cartRepository.save(cart), "bookId", "asc");
	}

	@Transactional(readOnly = true)
	public CartResponse getCart(Long customerId, String sortBy, String direction) {
		requirePositive(customerId, "customerId");
		Cart cart = cartRepository.findById(customerId)
				.orElseThrow(() -> new CartNotFoundException(customerId));
		return toResponse(cart, sortBy, direction);
	}

	public CartResponse updateItem(Long customerId, Long bookId, int quantity) {
		requirePositive(customerId, "customerId");
		requirePositive(bookId, "bookId");
		requirePositive(quantity, "quantity");
		Cart cart = findCart(customerId);
		CartItem item = cart.findItemByBookId(bookId)
				.orElseThrow(() -> new CartItemNotFoundException(bookId));
		item.setQuantity(quantity);
		return toResponse(cartRepository.save(cart), "bookId", "asc");
	}

	public void removeItem(Long customerId, Long bookId) {
		requirePositive(customerId, "customerId");
		requirePositive(bookId, "bookId");
		Cart cart = findCart(customerId);
		CartItem item = cart.findItemByBookId(bookId)
				.orElseThrow(() -> new CartItemNotFoundException(bookId));
		cart.removeItem(item);
		cartRepository.save(cart);
	}

	public void clearItems(Long customerId) {
		requirePositive(customerId, "customerId");
		Cart cart = findCart(customerId);
		cart.clearItems();
		cartRepository.save(cart);
	}

	private Cart findCart(Long customerId) {
		return cartRepository.findById(customerId)
				.orElseThrow(() -> new CartNotFoundException(customerId));
	}

	private void requirePositive(long value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be positive");
		}
	}

	private CartResponse toResponse(Cart cart, String sortBy, String direction) {
		Comparator<CartItem> comparator = switch (sortBy) {
			case "bookId" -> Comparator.comparing(CartItem::getBookId);
			case "quantity" -> Comparator.comparingInt(CartItem::getQuantity);
			default -> throw new IllegalArgumentException("sortBy must be 'bookId' or 'quantity'");
		};

		if ("desc".equalsIgnoreCase(direction)) {
			comparator = comparator.reversed();
		} else if (!"asc".equalsIgnoreCase(direction)) {
			throw new IllegalArgumentException("direction must be 'asc' or 'desc'");
		}

		List<CartItemResponse> items = cart.getItems().stream()
				.sorted(comparator)
				.map(item -> new CartItemResponse(item.getBookId(), item.getQuantity()))
				.toList();
		long totalQuantity = items.stream().mapToLong(CartItemResponse::quantity).sum();
		return new CartResponse(cart.getCustomerId(), items, totalQuantity);
	}
}
