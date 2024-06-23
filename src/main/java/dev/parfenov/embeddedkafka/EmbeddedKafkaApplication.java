package dev.parfenov.embeddedkafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"dev.parfenov.embeddedkafka.properties"})
public class EmbeddedKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmbeddedKafkaApplication.class, args);
    }

}
