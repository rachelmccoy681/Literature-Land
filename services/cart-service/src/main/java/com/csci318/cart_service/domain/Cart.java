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
@Table(name = "shopping_carts")
public class Cart {

	@Id
	private Long customerId;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("bookId ASC")
	private List<CartItem> items = new ArrayList<>();

	protected Cart() {
	}

	public Cart(Long customerId) {
		this.customerId = customerId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public Optional<CartItem> findItemByBookId(Long bookId) {
		return items.stream()
				.filter(item -> item.getBookId().equals(bookId))
				.findFirst();
	}

	public void addItem(CartItem item) {
		items.add(item);
		item.setCart(this);
	}

	public void removeItem(CartItem item) {
		items.remove(item);
		item.setCart(null);
	}

	public void clearItems() {
		items.forEach(item -> item.setCart(null));
		items.clear();
	}
}
