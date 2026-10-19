package com.csci318.chatbot_service.repository;

import com.csci318.chatbot_service.domain.ChatConversation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
}
