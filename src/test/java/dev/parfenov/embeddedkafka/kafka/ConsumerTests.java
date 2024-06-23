package dev.parfenov.embeddedkafka.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.parfenov.embeddedkafka.config.KafkaConfig;
import dev.parfenov.embeddedkafka.models.Message;
import dev.parfenov.embeddedkafka.properties.KafkaProperties;
import dev.parfenov.embeddedkafka.services.MessageService;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Stream;

import static dev.parfenov.embeddedkafka.config.KafkaConfig.KAFKA_TEMPLATE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DirtiesContext
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
@EmbeddedKafka(
        partitions = 1,
        topics = Topics.TEST_TOPIC
)
@SpringBootTest(classes = {
        Consumer.class,
        KafkaConfig.class,
        ObjectMapper.class
})
@EnableConfigurationProperties({
        KafkaProperties.class
})
public class ConsumerTests {

    @MockBean
    MessageService messageService;

    @SpyBean
    ObjectMapper objectMapper;
    @SpyBean
    Consumer consumer;

    @Autowired
    @Qualifier(KAFKA_TEMPLATE)
    KafkaTemplate<String, String> kafkaTemplate;

    @ParameterizedTest
    @MethodSource("messageSources")
    void testConsumer(Message msg) throws JsonProcessingException {
        var json = objectMapper.writeValueAsString(msg);

        kafkaTemplate.send(Topics.TEST_TOPIC, json);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(consumer, times(1)).handleMessage(eq(json));
            verify(objectMapper, times(1)).readValue(eq(json), eq(Message.class));
            verify(messageService, times(1)).process(eq(msg));
        });
    }

    private Stream<Arguments> messageSources() {
        return Stream.of(
                Arguments.of(new Message(UUID.randomUUID(), UUID.randomUUID().toString())),
                Arguments.of(new Message(UUID.randomUUID(), UUID.randomUUID().toString()))
        );
    }
}
