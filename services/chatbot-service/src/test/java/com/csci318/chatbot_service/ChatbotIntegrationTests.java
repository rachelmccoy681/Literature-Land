package com.csci318.chatbot_service;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.csci318.chatbot_service.FakeBackend.claudeMessage;
import static com.csci318.chatbot_service.FakeBackend.textReply;
import static com.csci318.chatbot_service.FakeBackend.toolCall;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// runs the whole chatbot (controller, langchain4j, chat memory, tools) against a fake claude api
// and fake bookstore services, so no api key or running services are needed
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "chatbot.anthropic.api-key=test-key",
        "chatbot.anthropic.max-retries=0",
        "spring.datasource.url=jdbc:h2:mem:chatbot-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ChatbotIntegrationTests {

    private static final FakeBackend backend = new FakeBackend();

    private static final String HOBBIT = """
            {"id": 2, "title": "The Hobbit", "author": "J.R.R. Tolkien", "genre": "Fantasy",
             "price": 20.0, "rating": 4.7, "stockQuantity": 5, "available": true,
             "description": "Bilbo Baggins joins a group of dwarves."}
            """;

    @DynamicPropertySource
    static void pointAtFakeBackend(DynamicPropertyRegistry registry) {
        registry.add("chatbot.anthropic.base-url", () -> backend.url() + "/v1/");
        registry.add("chatbot.services.stock-url", backend::url);
        registry.add("chatbot.services.cart-url", backend::url);
        registry.add("chatbot.services.order-url", backend::url);
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetBackend() {
        backend.reset();
    }

    @AfterAll
    static void stopBackend() {
        backend.reset();
    }

    private String chat(long customerId, String message) throws Exception {
        return mockMvc.perform(post("/api/chat/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"" + message + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void sendsHaikuRequestWithReadOnlyToolsAndThinkingOff() throws Exception {

        backend.claudeReplies(textReply("Hello! How can I help you find a book?"));

        chat(40, "Hi");

        String request = backend.claudeRequests().get(0);

        assertEquals("claude-haiku-5-5", JsonPath.read(request, "$.model"));
        assertEquals("disabled", JsonPath.read(request, "$.thinking.type"));
        assertEquals("low", JsonPath.read(request, "$.output_config.effort"));

        // haiku 5.5 rejects non-default sampling parameters
        assertTrue(JsonPath.<List<?>>read(request, "$..temperature").isEmpty());

        // the system prompt from resources is sent
        String system = JsonPath.<List<String>>read(request, "$.system[*].text").get(0);
        assertTrue(system.contains("Literature Land"));
        assertTrue(system.contains("You only answer questions"));

        // only read-only tools are offered
        List<String> tools = JsonPath.read(request, "$.tools[*].name");
        assertEquals(
                List.of("filterBooks", "getBookDetails", "getMyCart", "getMyOrderHistory",
                        "getMyWishlist", "getTrendingBooks", "searchBooks"),
                tools.stream().sorted().toList());

        // claude cannot choose whose cart to read: customer tools take no arguments
        Map<String, Object> cartSchema = JsonPath.<List<Map<String, Object>>>read(
                request, "$.tools[?(@.name == 'getMyCart')].input_schema").get(0);
        Object cartArguments = cartSchema.get("properties");
        assertTrue(cartArguments == null || ((Map<?, ?>) cartArguments).isEmpty());
    }

    @Test
    void answersUsingBookSearchResults() throws Exception {

        backend.serviceReplies("/books/search?keyword=hobbit", 200, "[" + HOBBIT + "]");
        backend.claudeReplies(claudeMessage("tool_use",
                "[" + toolCall("toolu_1", "searchBooks", "{\"keyword\": \"hobbit\"}") + "]"));
        backend.claudeReplies(textReply("Yes! The Hobbit by J.R.R. Tolkien is in stock for $20."));

        String response = chat(41, "Do you have The Hobbit?");

        assertEquals("Yes! The Hobbit by J.R.R. Tolkien is in stock for $20.", JsonPath.read(response, "$.reply"));
        assertEquals(List.of("/books/search?keyword=hobbit"), backend.serviceCalls());

        // the search result goes back to claude as the tool result
        String secondRequest = backend.claudeRequests().get(1);
        List<String> toolResults = JsonPath.read(
                secondRequest, "$.messages[*].content[?(@.type == 'tool_result')].content");
        assertEquals(1, toolResults.size());
        assertTrue(toolResults.get(0).contains("The Hobbit"));
        assertTrue(toolResults.get(0).contains("\"stockQuantity\":5"));
    }

    @Test
    void readsTheRequestingCustomersDataAndReportsServiceOutages() throws Exception {

        // customer 42 has no cart yet, and order service is down
        backend.serviceReplies("/api/carts/42", 404, "{\"error\": \"cart not found\"}");
        backend.serviceReplies("/trending-books", 503, "{\"error\": \"unavailable\"}");
        backend.claudeReplies(claudeMessage("tool_use", "["
                + toolCall("toolu_1", "getMyCart", "{}") + ","
                + toolCall("toolu_2", "getTrendingBooks", "{}") + "]"));
        backend.claudeReplies(textReply("Your cart is empty, and trending books are unavailable right now."));

        chat(42, "What's in my cart and what's popular?");

        assertTrue(backend.serviceCalls().contains("/api/carts/42"));

        String secondRequest = backend.claudeRequests().get(1);
        List<String> toolResults = JsonPath.read(
                secondRequest, "$.messages[*].content[?(@.type == 'tool_result')].content");
        assertTrue(toolResults.contains("The customer's cart is empty."));
        assertTrue(toolResults.stream().anyMatch(result -> result.contains("could not be reached")));
    }

    @Test
    void addsBookDetailsToOrderHistory() throws Exception {

        backend.serviceReplies("/orders/customer/43", 200, "[{\"id\": 7, \"customerId\": 43, \"status\": \"PENDING\"}]");
        backend.serviceReplies("/orders/7/items", 200, "[{\"id\": 1, \"orderId\": 7, \"bookId\": 2, \"quantity\": 3}]");
        backend.serviceReplies("/books/2/details", 200, HOBBIT);
        backend.claudeReplies(claudeMessage("tool_use",
                "[" + toolCall("toolu_1", "getMyOrderHistory", "{}") + "]"));
        backend.claudeReplies(textReply("You ordered 3 copies of The Hobbit."));

        chat(43, "What have I ordered?");

        String toolResult = JsonPath.<List<String>>read(
                backend.claudeRequests().get(1), "$.messages[*].content[?(@.type == 'tool_result')].content").get(0);

        assertEquals(7, (int) JsonPath.read(toolResult, "$[0].orderId"));
        assertEquals("The Hobbit", JsonPath.read(toolResult, "$[0].books[0].title"));
        assertEquals(3, (int) JsonPath.read(toolResult, "$[0].books[0].quantity"));
    }

    @Test
    void remembersEachCustomersConversationUntilCleared() throws Exception {

        backend.claudeReplies(textReply("Fantasy is a great choice."));
        backend.claudeReplies(textReply("Here are some more."));
        backend.claudeReplies(textReply("Hello!"));
        backend.claudeReplies(textReply("Hi there!"));

        chat(44, "I like fantasy");
        chat(44, "Any others?");

        // the second request carries the earlier exchange
        List<String> roles = JsonPath.read(backend.claudeRequests().get(1), "$.messages[*].role");
        assertEquals(List.of("user", "assistant", "user"), roles);

        // a different customer starts with an empty conversation
        chat(45, "Hello");
        assertEquals(1, JsonPath.<List<?>>read(backend.claudeRequests().get(2), "$.messages").size());

        // clearing forgets the conversation
        mockMvc.perform(delete("/api/chat/44")).andExpect(status().isNoContent());
        chat(44, "Hi");
        assertEquals(1, JsonPath.<List<?>>read(backend.claudeRequests().get(3), "$.messages").size());
    }

    @Test
    void returnsServiceUnavailableWhenClaudeFails() throws Exception {

        backend.claudeFails(401, "{\"type\": \"error\", \"error\": {\"type\": \"authentication_error\", \"message\": \"invalid x-api-key\"}}");

        mockMvc.perform(post("/api/chat/46")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Hi\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("the chatbot is unavailable right now, please try again later"));
    }

    @Test
    void repliesPolitelyWhenClaudeDeclines() throws Exception {

        backend.claudeReplies(claudeMessage("refusal", "[]"));

        String response = chat(47, "Something off-topic");

        assertTrue(JsonPath.<String>read(response, "$.reply").startsWith("Sorry, I can't help with that."));
    }

    @Test
    void rejectsInvalidRequestsWithoutCallingClaude() throws Exception {

        mockMvc.perform(post("/api/chat/48")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"   \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/chat/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Hi\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/chat/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Hi\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/chat/48")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"" + "a".repeat(2001) + "\"}"))
                .andExpect(status().isBadRequest());

        assertFalse(backend.claudeRequests().iterator().hasNext());
    }
}
