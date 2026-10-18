package com.csci318.cart_service.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "wishlists")
public class Wishlist {

	@Id
	private Long customerId;

	@OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("bookId ASC")
	private List<WishlistItem> items = new ArrayList<>();

	protected Wishlist() {
	}

	public Wishlist(Long customerId) {
		this.customerId = customerId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public List<WishlistItem> getItems() {
		return items;
	}

	public Optional<WishlistItem> findItemByBookId(Long bookId) {
		return items.stream()
				.filter(item -> item.getBookId().equals(bookId))
				.findFirst();
	}

	public void addItem(WishlistItem item) {
		items.add(item);
		item.setWishlist(this);
	}

	public void removeItem(WishlistItem item) {
		items.remove(item);
		item.setWishlist(null);
	}
}
