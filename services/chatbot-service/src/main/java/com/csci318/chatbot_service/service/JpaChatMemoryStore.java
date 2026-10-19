package com.csci318.chatbot_service.service;

import com.csci318.chatbot_service.domain.ChatConversation;
import com.csci318.chatbot_service.repository.ChatConversationRepository;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import org.springframework.stereotype.Component;

import java.util.List;

// saves each customer's conversation in the database so it survives restarts
@Component
public class JpaChatMemoryStore implements ChatMemoryStore {

    private final ChatConversationRepository chatConversationRepository;

    public JpaChatMemoryStore(ChatConversationRepository chatConversationRepository) {
        this.chatConversationRepository = chatConversationRepository;
    }

    @Override
    public List<ChatMessage> getMessages(Object customerId) {
        return chatConversationRepository.findById(toId(customerId))
                .map(conversation -> ChatMessageDeserializer.messagesFromJson(conversation.getMessagesJson()))
                .orElseGet(List::of);
    }

    @Override
    public void updateMessages(Object customerId, List<ChatMessage> messages) {
        String messagesJson = ChatMessageSerializer.messagesToJson(messages);

        ChatConversation conversation = chatConversationRepository.findById(toId(customerId))
                .orElseGet(() -> new ChatConversation(toId(customerId), messagesJson));

        conversation.setMessagesJson(messagesJson);

        chatConversationRepository.save(conversation);
    }

    @Override
    public void deleteMessages(Object customerId) {
        chatConversationRepository.deleteById(toId(customerId));
    }

    private Long toId(Object customerId) {
        return ((Number) customerId).longValue();
    }
}
