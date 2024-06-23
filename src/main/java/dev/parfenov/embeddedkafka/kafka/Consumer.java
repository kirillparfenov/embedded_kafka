package dev.parfenov.embeddedkafka.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.parfenov.embeddedkafka.config.KafkaConfig;
import dev.parfenov.embeddedkafka.models.Message;
import dev.parfenov.embeddedkafka.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class Consumer {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @SneakyThrows
    @KafkaListener(topics = Topics.TEST_TOPIC, containerFactory = KafkaConfig.LISTENER_CONTAINER)
    public void handleMessage(String message) {
        log.info("Из топика [{}] получено сообщение: {}", Topics.TEST_TOPIC, message);
        var msg = objectMapper.readValue(message, Message.class); //!должен быть обработчик ошибок
        messageService.process(msg);
    }
}
