package com.csci318.chatbot_service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

// one saved conversation per customer, stored as langchain4j's json message format
@Entity
public class ChatConversation {

    @Id
    private Long customerId;

    @Lob
    @Column(nullable = false)
    private String messagesJson;

    public ChatConversation() {
    }

    public ChatConversation(Long customerId, String messagesJson) {
        this.customerId = customerId;
        this.messagesJson = messagesJson;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getMessagesJson() {
        return messagesJson;
    }

    public void setMessagesJson(String messagesJson) {
        this.messagesJson = messagesJson;
    }
}
