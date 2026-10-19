package com.csci318.chatbot_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class CartClient {

    private final RestClient restClient;

    // connects to cart service
    public CartClient(@Value("${chatbot.services.cart-url}") String cartUrl) {
        this.restClient = RestClient.create(cartUrl);
    }

    // gets a customer's cart, or empty if they have not created one yet
    public Optional<Cart> getCart(long customerId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/carts/{customerId}", customerId)
                    .retrieve()
                    .body(Cart.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    // gets a customer's wishlist, or empty if they have not created one yet
    public Optional<Wishlist> getWishlist(long customerId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/api/wishlists/{customerId}", customerId)
                    .retrieve()
                    .body(Wishlist.class));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cart(Long customerId, List<CartItem> items, long totalQuantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CartItem(Long bookId, int quantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wishlist(Long customerId, List<WishlistItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WishlistItem(Long bookId) {
    }
}
