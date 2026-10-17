package com.csci318.cart_service.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_wishlist_item_book", columnNames = {"wishlist_id", "book_id"})
})
public class WishlistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "wishlist_id", nullable = false)
	private Wishlist wishlist;

	private Long bookId;

	protected WishlistItem() {
	}

	public WishlistItem(Long bookId) {
		this.bookId = bookId;
	}

	public Long getBookId() {
		return bookId;
	}

	void setWishlist(Wishlist wishlist) {
		this.wishlist = wishlist;
	}
}
