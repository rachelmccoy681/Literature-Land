package com.csci318.order_service;

import com.csci318.order_service.domain.StockAvailability;
import com.csci318.order_service.repository.StockAvailabilityRepository;
import com.csci318.order_service.service.TrendingBooksService;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// checks that order-service consumes events from a real (embedded) kafka broker
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.cloud.stream.kafka.binder.brokers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:order-kafka-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@EmbeddedKafka(partitions = 1, topics = {"stock-updates", "book-activity"})
class OrderKafkaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private StockAvailabilityRepository stockAvailabilityRepository;

    @Autowired
    private TrendingBooksService trendingBooksService;

    private Producer<String, String> producer;

    @BeforeEach
    void createProducer() {
        producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        ));
    }

    @AfterEach
    void closeProducer() {
        producer.close();
    }

    // sends json in the same shape stock-service publishes
    private void send(String topic, String json) throws Exception {
        producer.send(new ProducerRecord<>(topic, json)).get();
    }

    private Optional<Integer> storedQuantity(Long bookId) {
        return stockAvailabilityRepository.findById(bookId)
                .map(StockAvailability::getStockQuantity);
    }

    @Test
    void savesStockUpdatesFromKafka() throws Exception {

        send("stock-updates", """
                {"bookId": 101, "stockQuantity": 7}
                """);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertEquals(Optional.of(7), storedQuantity(101L)));

        // a later event overwrites the stored quantity
        send("stock-updates", """
                {"bookId": 101, "stockQuantity": 0}
                """);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertEquals(Optional.of(0), storedQuantity(101L)));

        mockMvc.perform(get("/stock-availability/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void countsSearchAndViewActivityFromKafka() throws Exception {

        long now = System.currentTimeMillis();

        // unknown activity types are ignored
        send("book-activity", """
                {"bookId": 202, "activityType": "PURCHASE", "timestamp": %d}
                """.formatted(now));
        send("book-activity", """
                {"bookId": 202, "activityType": "SEARCH", "timestamp": %d}
                """.formatted(now));
        send("book-activity", """
                {"bookId": 202, "activityType": "VIEW", "timestamp": %d}
                """.formatted(now));

        // single partition keeps order, so the ignored event has been handled by now
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertEquals(2, trendingBooksService.getActivityCount(202L)));

        // checked through the service because the controller also calls stock-service over http
        List<Map<String, Object>> trendingBooks = trendingBooksService.getTrendingBooks();

        assertEquals(202L, trendingBooks.get(0).get("bookId"));
        assertEquals(2, trendingBooks.get(0).get("activityCount"));
    }
}
