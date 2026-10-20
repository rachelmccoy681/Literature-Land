package com.csci318.chatbot_service.assistant;

import com.csci318.chatbot_service.client.CartClient;
import com.csci318.chatbot_service.client.OrderClient;
import com.csci318.chatbot_service.client.StockClient;
import com.csci318.chatbot_service.client.StockClient.Book;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.function.Supplier;

// read-only tools claude can call to answer customer questions;
// tools about the customer get their id from the conversation, never from claude
@Component
public class BookstoreTools {

    private static final Logger log = LoggerFactory.getLogger(BookstoreTools.class);

    static final String SERVICE_UNAVAILABLE =
            "A bookstore service could not be reached, so this information is unavailable right now.";

    private final StockClient stockClient;
    private final CartClient cartClient;
    private final OrderClient orderClient;
    private final JsonMapper jsonMapper;

    public BookstoreTools(
            StockClient stockClient,
            CartClient cartClient,
            OrderClient orderClient,
            JsonMapper jsonMapper) {
        this.stockClient = stockClient;
        this.cartClient = cartClient;
        this.orderClient = orderClient;
        this.jsonMapper = jsonMapper;
    }

    @Tool("Searches the catalogue for books whose title, author or genre contains a keyword")
    public String searchBooks(@P("a word or phrase from a book title, author name or genre") String keyword) {
        return respond(() -> stockClient.searchBooks(keyword), "No books matched that keyword.");
    }

    @Tool("Finds books that match filters. Only pass the filters the customer asked about.")
    public String filterBooks(
            @P(value = "genre, for example Fantasy, Horror, Romance or Young Adult", required = false) String genre,
            @P(value = "author name", required = false) String author,
            @P(value = "lowest price in dollars", required = false) Double minPrice,
            @P(value = "highest price in dollars", required = false) Double maxPrice,
            @P(value = "true to only include books that are in stock", required = false) Boolean availableOnly,
            @P(value = "lowest rating out of 5", required = false) Double minRating) {

        return respond(
                () -> stockClient.filterBooks(genre, author, minPrice, maxPrice, availableOnly, minRating),
                "No books matched those filters."
        );
    }

    @Tool("Gets the full details of one book, including its description and how many copies are in stock")
    public String getBookDetails(@P("the book's id") long bookId) {
        return respond(() -> stockClient.getBookDetails(bookId), "There is no book with id " + bookId + ".");
    }

    @Tool("Lists the books customers have searched for and viewed most in the last 24 hours")
    public String getTrendingBooks() {
        return respond(orderClient::getTrendingBooks, "No books are trending right now.");
    }

    @Tool("Shows the books in the current customer's cart")
    public String getMyCart(@ToolMemoryId Long customerId) {
        return respond(() -> cartClient.getCart(customerId)
                        .map(cart -> new CartView(
                                cart.items().stream()
                                        .map(item -> new BookLine(findBook(item.bookId()), item.quantity()))
                                        .toList(),
                                cart.totalQuantity()))
                        .orElse(null),
                "The customer's cart is empty.");
    }

    @Tool("Shows the books on the current customer's wishlist")
    public String getMyWishlist(@ToolMemoryId Long customerId) {
        return respond(() -> cartClient.getWishlist(customerId)
                        .map(wishlist -> wishlist.items().stream()
                                .map(item -> new BookLine(findBook(item.bookId()), 1))
                                .toList())
                        .orElse(null),
                "The customer's wishlist is empty.");
    }

    @Tool("Shows the current customer's past orders and the books in each one")
    public String getMyOrderHistory(@ToolMemoryId Long customerId) {
        return respond(() -> orderClient.getOrderHistory(customerId).stream()
                        .map(order -> new OrderView(
                                order.id(),
                                order.status(),
                                orderClient.getOrderItems(order.id()).stream()
                                        .map(item -> new BookLine(findBook(item.bookId()), item.quantity()))
                                        .toList()))
                        .toList(),
                "The customer has not placed any orders yet.");
    }

    // runs a lookup and turns the result into text for claude, including when nothing is found or a service is down
    private String respond(Supplier<Object> lookup, String emptyMessage) {
        try {
            Object result = lookup.get();

            if (result == null || (result instanceof List<?> list && list.isEmpty())) {
                return emptyMessage;
            }

            return jsonMapper.writeValueAsString(result);

        } catch (HttpClientErrorException.NotFound e) {
            return emptyMessage;

        } catch (RestClientException e) {
            log.warn("chatbot tool could not reach a bookstore service: {}", e.getMessage());
            return SERVICE_UNAVAILABLE;
        }
    }

    // adds title, author and price to a book id from the cart, wishlist or orders
    private Book findBook(Long bookId) {
        try {
            return stockClient.getBookDetails(bookId);
        } catch (RestClientException e) {
            return new Book(bookId, null, null, null, null, null, null, null, null);
        }
    }

    record BookLine(Long bookId, String title, String author, Double price, int quantity) {
        BookLine(Book book, int quantity) {
            this(book.id(), book.title(), book.author(), book.price(), quantity);
        }
    }

    record CartView(List<BookLine> books, long totalQuantity) {
    }

    record OrderView(Long orderId, String status, List<BookLine> books) {
    }
}
