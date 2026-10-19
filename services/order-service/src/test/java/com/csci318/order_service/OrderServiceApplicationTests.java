package com.csci318.order_service;

import com.csci318.order_service.client.StockClient;
import com.csci318.order_service.repository.OrderRepository;
import com.csci318.order_service.repository.OrderItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
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

    @Autowired
    private OrderItemRepository orderItemRepository;

    // simulates the stock service during testing
    @MockitoBean
    private StockClient stockClient;

    @BeforeEach
    void clearOrders() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createsOrderSuccessfully() throws Exception {

        // simulate successful stock reduction
        doNothing().when(stockClient).reduceStockForOrder(anyList());

        String orderBody = """
                [
                    {
                        "bookId": 3,
                        "quantity": 2
                    },
                    {
                        "bookId": 1,
                        "quantity": 1
                    }
                ]
                """;

        mockMvc.perform(post("/orders")
                        .param("customerId", "25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerId").value(25))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void returnsCustomerOrderHistory() throws Exception {

        doNothing().when(stockClient).reduceStockForOrder(anyList());

        String orderBody = """
                [
                    {
                        "bookId": 1,
                        "quantity": 2
                    }
                ]
                """;

        // create an order
        mockMvc.perform(post("/orders")
                        .param("customerId", "25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andExpect(status().isOk());

        // check customer order history
        mockMvc.perform(get("/orders/customer/25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value(25))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void rejectsOrdersWithoutValidItems() throws Exception {

        // empty order
        mockMvc.perform(post("/orders")
                        .param("customerId", "25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        // invalid quantity
        mockMvc.perform(post("/orders")
                        .param("customerId", "25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "bookId": 1,
                                        "quantity": 0
                                    }
                                ]
                                """))
                .andExpect(status().isBadRequest());

        // invalid customer ID
        mockMvc.perform(post("/orders")
                        .param("customerId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "bookId": 1,
                                        "quantity": 1
                                    }
                                ]
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsEmptyHistoryForNewCustomer() throws Exception {

        mockMvc.perform(get("/orders/customer/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}