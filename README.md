_Модель [Message](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/main/java/dev/parfenov/embeddedkafka/models/Message.java):_
```java
public class Message {
    private UUID id;
    private String title;
}
```

--------

_[Консьюмер](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/main/java/dev/parfenov/embeddedkafka/kafka/Consumer.java), принимающий Message в виде json:_
```java
public class Consumer {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @SneakyThrows
    @KafkaListener(topics = Topics.TEST_TOPIC, containerFactory = KafkaConfig.LISTENER_CONTAINER)
    public void handleMessage(String message) {
        log.info("Из топика [{}] получено сообщение: {}", Topics.TEST_TOPIC, message);
        var msg = objectMapper.readValue(message, Message.class);
        messageService.process(msg);
    }
}
```

--------

_[MessageService](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/main/java/dev/parfenov/embeddedkafka/services/MessageService.java) для обработки Message:_
```java
public class MessageService {

    public void process(Message msg) {
        //логика обработки сообщения
    }
}
```

--------

_[Конфигурация](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/main/java/dev/parfenov/embeddedkafka/config/KafkaConfig.java) Kafka:_
```java
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
```
**@EnableKafka** - без этой аннотации приложение не увидит @KafkaListener

--------

_Пропсы для теста в [application-test.yaml](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/test/resources/application-test.yaml):_
```
kafka:
  servers: ${spring.embedded.kafka.brokers}
  reset: earliest
  group-id: test_group_id
```
**${spring.embedded.kafka.brokers}** - обязательно, чтобы консьюмеры и продюсеры смотрели в одно место

--------

_[ConsumerTests](https://github.com/kirillparfenov/embedded_kafka/blob/develop/src/test/java/dev/parfenov/embeddedkafka/kafka/ConsumerTests.java) для теста Consumer:_
```java
@DirtiesContext
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
@EmbeddedKafka(
        partitions = 1,
        topics = Topics.TEST_TOPIC
)
@SpringBootTest(classes = {
        KafkaConfig.class
})
@EnableConfigurationProperties({
        KafkaProperties.class
})
public class ConsumerTests {}
```

**@TestInstance(PER_CLASS)** - дает возможность создать метод для @MethodSource не статическим


```java
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
```