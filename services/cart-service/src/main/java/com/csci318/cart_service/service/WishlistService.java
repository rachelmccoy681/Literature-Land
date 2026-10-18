package com.csci318.cart_service.service;

import com.csci318.cart_service.api.WishlistItemResponse;
import com.csci318.cart_service.api.WishlistResponse;
import com.csci318.cart_service.domain.Wishlist;
import com.csci318.cart_service.domain.WishlistItem;
import com.csci318.cart_service.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class WishlistService {

	private final WishlistRepository wishlistRepository;

	public WishlistService(WishlistRepository wishlistRepository) {
		this.wishlistRepository = wishlistRepository;
	}

	public WishlistResponse addItem(Long customerId, Long bookId) {
		requirePositive(customerId, "customerId");
		requirePositive(bookId, "bookId");
		Wishlist wishlist = wishlistRepository.findById(customerId)
				.orElseGet(() -> new Wishlist(customerId));
		if (wishlist.findItemByBookId(bookId).isEmpty()) {
			wishlist.addItem(new WishlistItem(bookId));
		}
		return toResponse(wishlistRepository.save(wishlist));
	}

	@Transactional(readOnly = true)
	public WishlistResponse getWishlist(Long customerId) {
		requirePositive(customerId, "customerId");
		Wishlist wishlist = wishlistRepository.findById(customerId)
				.orElseThrow(() -> new WishlistNotFoundException(customerId));
		return toResponse(wishlist);
	}

	public void removeItem(Long customerId, Long bookId) {
		requirePositive(customerId, "customerId");
		requirePositive(bookId, "bookId");
		Wishlist wishlist = wishlistRepository.findById(customerId)
				.orElseThrow(() -> new WishlistNotFoundException(customerId));
		WishlistItem item = wishlist.findItemByBookId(bookId)
				.orElseThrow(() -> new WishlistItemNotFoundException(bookId));
		wishlist.removeItem(item);
		wishlistRepository.save(wishlist);
	}

	private void requirePositive(long value, String name) {
		if (value <= 0) {
			throw new IllegalArgumentException(name + " must be positive");
		}
	}

	private WishlistResponse toResponse(Wishlist wishlist) {
		List<WishlistItemResponse> items = wishlist.getItems().stream()
				.sorted(Comparator.comparing(WishlistItem::getBookId))
				.map(item -> new WishlistItemResponse(item.getBookId()))
				.toList();
		return new WishlistResponse(wishlist.getCustomerId(), items);
	}
}
