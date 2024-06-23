package dev.parfenov.embeddedkafka.config;

import dev.parfenov.embeddedkafka.properties.KafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    public static final String LISTENER_CONTAINER = "LISTENER_CONTAINER";
    public static final String KAFKA_TEMPLATE = "KAFKA_TEMPLATE";

    private static final String PRODUCER_FACTORY = "PRODUCER_FACTORY";
    private static final String CONSUMER_FACTORY = "CONSUMER_FACTORY";

    private final KafkaProperties kafkaProperties;

    @Bean(PRODUCER_FACTORY)
    public ProducerFactory<String, String> producerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(CONSUMER_FACTORY)
    public ConsumerFactory<String, String> consumerFactory() {
        var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getReset());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean(LISTENER_CONTAINER)
    public ConcurrentKafkaListenerContainerFactory<String, String> listenerContainerFactory(
            @Qualifier(CONSUMER_FACTORY) ConsumerFactory<String, String> consumerFactory
    ) {
        var container = new ConcurrentKafkaListenerContainerFactory<String, String>();
        container.setConsumerFactory(consumerFactory);
        return container;
    }

    @Bean(KAFKA_TEMPLATE)
    public KafkaTemplate<String, String> kafkaTemplate(
            @Qualifier(PRODUCER_FACTORY) ProducerFactory<String, String> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
