package br.com.desafiojava.event;

import br.com.desafiojava.common.exception.KafkaProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class OrderProcessingConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersProcessedTopic;

    public OrderProcessingConsumer(KafkaTemplate<String, Object> kafkaTemplate,
                                   @Value("${kafka.topics.orders-processed}") String ordersProcessedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersProcessedTopic = ordersProcessedTopic;
    }

    @KafkaListener(topics = "${kafka.topics.orders-to-process}", groupId = "order-processing-group")
    public void processOrder(
            @Payload(required = false) OrderProcessingEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset,
            ConsumerRecordMetadata metadata) {

        try {

            BigDecimal totalAmount = event.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrderProcessedEvent processedEvent = OrderProcessedEvent.builder()
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .totalAmount(totalAmount)
                    .items(event.getItems())
                    .build();

            publishEvent(event, processedEvent);

        } catch (KafkaProcessingException e) {
            log.error("Processing error for order event - Topic: {}, Key: {}, Offset: {}, Error: {}",
                    topic, key, offset, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing order event - Topic: {}, Key: {}, Offset: {}, Error: {}",
                    topic, key, offset, e.getMessage(), e);
            throw new KafkaProcessingException("Unexpected error processing order: " + e.getMessage());
        }
    }

    private void publishEvent(OrderProcessingEvent event, OrderProcessedEvent processedEvent) {
        try {
            kafkaTemplate.send(ordersProcessedTopic, processedEvent.getOrderId(), processedEvent);
            log.info("Successfully processed order {} and published to topic {}", event.getOrderId(), ordersProcessedTopic);
        } catch (Exception e) {
            log.error("Failed to publish processed event for order {}: {}", event.getOrderId(), e.getMessage());
            throw new KafkaProcessingException("Failed to publish processed event: " + e.getMessage());
        }
    }
}