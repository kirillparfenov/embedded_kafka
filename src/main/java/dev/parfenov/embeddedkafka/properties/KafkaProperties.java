package dev.parfenov.embeddedkafka.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    private String servers;
    private String reset;
    private String groupId;
}
