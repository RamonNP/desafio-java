package br.com.desafiojava.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
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

        if (event == null) {
            //log.error("Erro de desserialização na mensagem - Topic: {}, Key: {}, Offset: {}", topic, key, offset);
            //return;
        }
        BigDecimal totalAmount = event.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderProcessedEvent processedEvent = new OrderProcessedEvent(
                event.getOrderId(),
                event.getCustomerId(),
                totalAmount
        );

        kafkaTemplate.send(ordersProcessedTopic, processedEvent.getOrderId().toString(), processedEvent);
    }
}