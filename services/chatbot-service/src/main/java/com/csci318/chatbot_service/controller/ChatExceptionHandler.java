package com.csci318.chatbot_service.controller;

import com.csci318.chatbot_service.service.ChatbotUnavailableException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
public class ChatExceptionHandler {

    // invalid customer id or message
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    // missing or malformed request body, or a customer id that isn't a number
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> handleUnreadableRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "request must include a numeric customer ID and a JSON body with a message"));
    }

    // claude could not be reached or rejected the request
    @ExceptionHandler(ChatbotUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(ChatbotUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
    }
}
