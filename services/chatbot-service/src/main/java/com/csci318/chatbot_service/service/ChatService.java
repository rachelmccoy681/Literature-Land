package com.csci318.chatbot_service.service;

import com.csci318.chatbot_service.assistant.BookstoreAssistant;

import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    static final int MAX_MESSAGE_LENGTH = 2000;

    static final String NO_ANSWER_REPLY =
            "Sorry, I can't help with that. I can help you find books or explain how to use the store.";

    private final BookstoreAssistant bookstoreAssistant;
    private final ChatMemoryStore chatMemoryStore;

    public ChatService(BookstoreAssistant bookstoreAssistant, ChatMemoryStore chatMemoryStore) {
        this.bookstoreAssistant = bookstoreAssistant;
        this.chatMemoryStore = chatMemoryStore;
    }

    // sends a customer's message to the chatbot and returns its reply
    public String chat(Long customerId, String message) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer ID must be a positive number");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be empty");
        }

        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException(
                    "message must be at most " + MAX_MESSAGE_LENGTH + " characters");
        }

        String reply;

        try {
            reply = bookstoreAssistant.chat(customerId, message.strip());
        } catch (RuntimeException e) {
            log.error("chatbot could not get a reply from claude", e);
            throw new ChatbotUnavailableException();
        }

        // an empty reply happens when claude declines a request
        if (reply == null || reply.isBlank()) {
            return NO_ANSWER_REPLY;
        }

        return reply.strip();
    }

    // forgets a customer's conversation so the next message starts fresh
    public void clearConversation(Long customerId) {

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("customer ID must be a positive number");
        }

        chatMemoryStore.deleteMessages(customerId);
    }
}
