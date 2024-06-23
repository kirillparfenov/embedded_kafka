package dev.parfenov.embeddedkafka.services;

import dev.parfenov.embeddedkafka.models.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    public void process(Message msg) {
        //логика обработки сообщения
    }
}
