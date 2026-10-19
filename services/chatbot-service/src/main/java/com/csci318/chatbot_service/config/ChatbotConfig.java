package com.csci318.chatbot_service.config;

import com.csci318.chatbot_service.assistant.BookstoreAssistant;
import com.csci318.chatbot_service.assistant.BookstoreTools;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class ChatbotConfig {

    // connects langchain4j to claude
    @Bean
    public ChatModel chatModel(
            @Value("${chatbot.anthropic.api-key}") String apiKey,
            @Value("${chatbot.anthropic.base-url}") String baseUrl,
            @Value("${chatbot.anthropic.model}") String model,
            @Value("${chatbot.anthropic.effort}") String effort,
            @Value("${chatbot.anthropic.max-tokens}") int maxTokens,
            @Value("${chatbot.anthropic.max-retries}") int maxRetries) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY is not set. Set it in the environment before starting the chatbot service."
            );
        }

        return AnthropicChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(maxTokens)
                .maxRetries(maxRetries)
                .timeout(Duration.ofSeconds(60))
                // thinking stays off: answering questions doesn't need it, and the chat memory
                // trims old messages, which would invalidate thinking blocks from earlier turns
                .thinkingType("disabled")
                .customParameters(Map.of("output_config", Map.of("effort", effort)))
                .build();
    }

    // builds the chatbot from the model, its tools and per-customer chat memory
    @Bean
    public BookstoreAssistant bookstoreAssistant(
            ChatModel chatModel,
            BookstoreTools bookstoreTools,
            ChatMemoryStore chatMemoryStore,
            @Value("${chatbot.memory.max-messages}") int maxMessages) {

        return AiServices.builder(BookstoreAssistant.class)
                .chatModel(chatModel)
                .tools(bookstoreTools)
                .chatMemoryProvider(customerId -> MessageWindowChatMemory.builder()
                        .id(customerId)
                        .maxMessages(maxMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .maxSequentialToolsInvocations(10)
                .build();
    }
}
