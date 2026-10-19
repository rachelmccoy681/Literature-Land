package com.csci318.stock_service;

import com.jayway.jsonpath.JsonPath;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// checks that stock-service publishes events to a real (embedded) kafka broker
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "spring.cloud.stream.kafka.binder.brokers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:stock-kafka-test;DB_CLOSE_DELAY=-1"
})
@EmbeddedKafka(partitions = 1, topics = {"stock-updates", "book-activity"})
class StockKafkaIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private Consumer<String, String> consumer;

    @AfterEach
    void closeConsumer() {
        if (consumer != null) {
            consumer.close();
        }
    }

    // listens to a topic from its current end so each test only sees its own events
    private void listenTo(String topic) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );

        consumer = new KafkaConsumer<>(props);
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, true, topic);
    }

    @Test
    void publishesStockUpdateWhenStockIsReduced() throws Exception {

        listenTo("stock-updates");

        String response = mockMvc.perform(post("/books/1/reduce-stock")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        int remainingStock = JsonPath.read(response, "$.stockQuantity");

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "stock-updates", Duration.ofSeconds(15));

        assertEquals(1, (int) JsonPath.read(record.value(), "$.bookId"));
        assertEquals(remainingStock, (int) JsonPath.read(record.value(), "$.stockQuantity"));
    }

    @Test
    void doesNotPublishStockUpdateWhenReductionFails() throws Exception {

        listenTo("stock-updates");

        // more copies than any sample book has, so the transaction is rejected
        mockMvc.perform(post("/books/1/reduce-stock")
                        .param("quantity", "1000"))
                .andExpect(status().isBadRequest());

        ConsumerRecords<String, String> records =
                KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(3));

        assertTrue(records.isEmpty());
    }

    @Test
    void publishesViewActivityWhenBookIsViewed() throws Exception {

        listenTo("book-activity");

        mockMvc.perform(get("/books/2"))
                .andExpect(status().isOk());

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "book-activity", Duration.ofSeconds(15));

        assertEquals(2, (int) JsonPath.read(record.value(), "$.bookId"));
        assertEquals("VIEW", JsonPath.read(record.value(), "$.activityType"));
        assertTrue(((Number) JsonPath.read(record.value(), "$.timestamp")).longValue() > 0);
    }

    @Test
    void publishesSearchActivityForEachMatchingBook() throws Exception {

        listenTo("book-activity");

        // matches only "The Hobbit" in the sample data
        mockMvc.perform(get("/books/search")
                        .param("keyword", "hobbit"))
                .andExpect(status().isOk());

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, "book-activity", Duration.ofSeconds(15));

        assertEquals(2, (int) JsonPath.read(record.value(), "$.bookId"));
        assertEquals("SEARCH", JsonPath.read(record.value(), "$.activityType"));
    }
}
