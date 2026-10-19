package com.csci318.chatbot_service.service;

public class ChatbotUnavailableException extends RuntimeException {

    public ChatbotUnavailableException() {
        super("the chatbot is unavailable right now, please try again later");
    }
}
