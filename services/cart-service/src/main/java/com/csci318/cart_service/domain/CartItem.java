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
@Table(name = "cart_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_cart_item_book", columnNames = {"cart_id", "book_id"})
})
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	private Long bookId;

	private int quantity;

	protected CartItem() {
	}

	public CartItem(Long bookId, int quantity) {
		this.bookId = bookId;
		this.quantity = quantity;
	}

	public Long getBookId() {
		return bookId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void increaseQuantity(int amount) {
		quantity = Math.addExact(quantity, amount);
	}

	void setCart(Cart cart) {
		this.cart = cart;
	}
}
