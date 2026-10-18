package com.csci318.cart_service.api;

import com.csci318.cart_service.service.CartItemNotFoundException;
import com.csci318.cart_service.service.CartNotFoundException;
import com.csci318.cart_service.service.WishlistItemNotFoundException;
import com.csci318.cart_service.service.WishlistNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CartExceptionHandler {

	@ExceptionHandler({
			CartNotFoundException.class,
			CartItemNotFoundException.class,
			WishlistNotFoundException.class,
			WishlistItemNotFoundException.class
	})
	public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiError(exception.getMessage()));
	}

	@ExceptionHandler({
			IllegalArgumentException.class,
			ArithmeticException.class,
			MethodArgumentNotValidException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
		String message = exception instanceof IllegalArgumentException
				|| exception instanceof ArithmeticException
				? exception.getMessage()
				: "Request contains invalid or missing values";
		return ResponseEntity.badRequest().body(new ApiError(message));
	}

	public record ApiError(String message) {
	}
}
