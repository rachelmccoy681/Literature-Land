package com.csci318.cart_service.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(@NotNull @Positive Integer quantity) {
}
