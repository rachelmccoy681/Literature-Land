package com.csci318.cart_service;

import com.csci318.cart_service.repository.CartRepository;
import com.csci318.cart_service.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class CartServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private WishlistRepository wishlistRepository;

	@BeforeEach
	void clearCartsAndWishlists() {
		cartRepository.deleteAll();
		wishlistRepository.deleteAll();
	}

	@Test
	void addsItemsIncrementsExistingQuantityAndReturnsSortedCart() throws Exception {
		addItem(25, 3, 2);
		addItem(25, 1, 4);
		addItem(25, 3, 1);

		mockMvc.perform(get("/api/carts/25")
						.param("sortBy", "quantity")
						.param("direction", "desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(25))
				.andExpect(jsonPath("$.totalQuantity").value(7))
				.andExpect(jsonPath("$.items[0].bookId").value(1))
				.andExpect(jsonPath("$.items[0].quantity").value(4))
				.andExpect(jsonPath("$.items[1].bookId").value(3))
				.andExpect(jsonPath("$.items[1].quantity").value(3));
	}

	@Test
	void updatesAndRemovesItems() throws Exception {
		addItem(25, 3, 2);

		mockMvc.perform(put("/api/carts/25/items/3")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"quantity\":5}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].quantity").value(5));

		mockMvc.perform(delete("/api/carts/25/items/3"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/carts/25"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.totalQuantity").value(0));
	}

	@Test
	void rejectsInvalidQuantitiesAndReportsMissingCartItems() throws Exception {
		mockMvc.perform(post("/api/carts/25/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\":3,\"quantity\":0}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(delete("/api/carts/25/items/3"))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsUnsupportedSortOptions() throws Exception {
		addItem(25, 3, 2);

		mockMvc.perform(get("/api/carts/25").param("sortBy", "title"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsNonPositivePathIds() throws Exception {
		mockMvc.perform(get("/api/carts/0"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void addsListsAndRemovesWishlistItemsWithoutDuplicates() throws Exception {
		addWishlistItem(25, 3);
		addWishlistItem(25, 1);
		addWishlistItem(25, 3);

		mockMvc.perform(get("/api/wishlists/25"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId").value(25))
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].bookId").value(1))
				.andExpect(jsonPath("$.items[1].bookId").value(3));

		mockMvc.perform(delete("/api/wishlists/25/items/3"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/wishlists/25"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].bookId").value(1));
	}

	@Test
	void rejectsInvalidWishlistRequestsAndReportsMissingWishlistItems() throws Exception {
		mockMvc.perform(post("/api/wishlists/25/items")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\":0}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/wishlists/25"))
				.andExpect(status().isNotFound());

		addWishlistItem(25, 1);
		mockMvc.perform(delete("/api/wishlists/25/items/3"))
				.andExpect(status().isNotFound());
	}

	@Test
	void rejectsNonPositiveWishlistPathIds() throws Exception {
		mockMvc.perform(get("/api/wishlists/0"))
				.andExpect(status().isBadRequest());
	}

	private void addItem(long customerId, long bookId, int quantity) throws Exception {
		mockMvc.perform(post("/api/carts/{customerId}/items", customerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\":" + bookId + ",\"quantity\":" + quantity + "}"))
				.andExpect(status().isOk());
	}

	private void addWishlistItem(long customerId, long bookId) throws Exception {
		mockMvc.perform(post("/api/wishlists/{customerId}/items", customerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\":" + bookId + "}"))
				.andExpect(status().isOk());
	}
}
