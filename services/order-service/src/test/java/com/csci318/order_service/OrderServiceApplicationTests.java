package com.csci318.order_service;

import com.csci318.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:order-history-test;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrderServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void clearOrders() {
		orderRepository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createsOrdersAndReturnsCustomerHistoryNewestFirst() throws Exception {
		String orderBody = """
				{"customerId":25,"items":[{"bookId":3,"quantity":2},{"bookId":1,"quantity":1}]}
				""";
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(orderBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderId").isNumber())
				.andExpect(jsonPath("$.customerId").value(25))
				.andExpect(jsonPath("$.createdAt").isNotEmpty())
				.andExpect(jsonPath("$.totalQuantity").value(3))
				.andExpect(jsonPath("$.items[0].bookId").value(1))
				.andExpect(jsonPath("$.items[0].quantity").value(1))
				.andExpect(jsonPath("$.items[1].bookId").value(3));

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerId\":25,\"items\":[{\"bookId\":9,\"quantity\":1}]}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/orders/25"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].customerId").value(25))
				.andExpect(jsonPath("$[0].items[0].bookId").value(9))
				.andExpect(jsonPath("$[1].items.length()").value(2))
				.andExpect(jsonPath("$[1].items[0].bookId").value(1));

		mockMvc.perform(get("/api/orders/26"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void rejectsOrdersWithoutValidItems() throws Exception {
		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerId\":25,\"items\":[]}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"customerId\":25,\"items\":[{\"bookId\":1,\"quantity\":0}]}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/orders/0"))
				.andExpect(status().isBadRequest());
	}
}
