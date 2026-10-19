package com.csci318.chatbot_service.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

// langchain4j generates the implementation of this interface
public interface BookstoreAssistant {

    // answers one customer message, using that customer's saved conversation
    @SystemMessage(fromResource = "/prompts/bookstore-assistant.txt")
    String chat(@MemoryId Long customerId, @UserMessage String message);
}
