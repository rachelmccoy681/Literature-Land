package com.csci318.chatbot_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"chatbot.anthropic.api-key=test-key",
		"spring.datasource.url=jdbc:h2:mem:chatbot-context-test;DB_CLOSE_DELAY=-1"
})
class ChatbotServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
