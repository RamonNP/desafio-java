package br.com.desafiojava.config;

import br.com.desafiojava.common.exception.OrderProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.producer.retries:3}")
    private int retries;

    @Value("${kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${kafka.producer.linger-ms:1}")
    private int lingerMs;

    @Value("${kafka.producer.acks:all}")
    private String acks;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        try {
            // Validate bootstrap servers
            if (bootstrapServers == null || bootstrapServers.trim().isEmpty()) {
                log.error("Kafka bootstrap servers configuration is missing or empty");
                throw new IllegalArgumentException("Kafka bootstrap servers cannot be null or empty");
            }

            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

            // Additional producer configurations for reliability
            configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
            configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
            configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
            configProps.put(ProducerConfig.ACKS_CONFIG, acks);
            configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Enable idempotent producer
            configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // Recommended with idempotence

            log.info("Initializing Kafka ProducerFactory with bootstrap servers: {}", bootstrapServers);
            return new DefaultKafkaProducerFactory<>(configProps);
        } catch (Exception e) {
            log.error("Failed to create Kafka ProducerFactory: {}", e.getMessage(), e);
            throw new OrderProcessingException("Unable to initialize Kafka ProducerFactory", e);
        }
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        try {
            KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
            log.info("Successfully initialized KafkaTemplate");
            return kafkaTemplate;
        } catch (Exception e) {
            log.error("Failed to create KafkaTemplate: {}", e.getMessage(), e);
            throw new OrderProcessingException("Unable to initialize KafkaTemplate", e);
        }
    }
}