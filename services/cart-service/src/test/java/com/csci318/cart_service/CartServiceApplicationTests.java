package com.csci318.cart_service;

import com.csci318.cart_service.repository.CartRepository;
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

	@BeforeEach
	void clearCarts() {
		cartRepository.deleteAll();
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

	private void addItem(long customerId, long bookId, int quantity) throws Exception {
		mockMvc.perform(post("/api/carts/{customerId}/items", customerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"bookId\":" + bookId + ",\"quantity\":" + quantity + "}"))
				.andExpect(status().isOk());
	}
}
