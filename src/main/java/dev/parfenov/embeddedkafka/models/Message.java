package dev.parfenov.embeddedkafka.models;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private UUID id;
    private String title;
}
