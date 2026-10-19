package com.csci318.chatbot_service.controller;

import com.csci318.chatbot_service.service.ChatService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // sends a message to the chatbot and returns its reply
    @PostMapping("/{customerId}")
    public ChatResponse chat(
            @PathVariable Long customerId,
            @RequestBody ChatRequest request) {

        String reply = chatService.chat(customerId, request.message());

        return new ChatResponse(customerId, reply);
    }

    // clears the customer's conversation
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> clearConversation(@PathVariable Long customerId) {

        chatService.clearConversation(customerId);

        return ResponseEntity.noContent().build();
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(Long customerId, String reply) {
    }
}
