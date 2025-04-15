package br.com.desafiojava.event;

import br.com.desafiojava.application.OrderCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final String ordersDlqTopic;
    private final OrderCalculationService orderCalculationService;

    public OrderProcessingConsumer(KafkaTemplate<String, Object> kafkaTemplate,
                                   @Value("${kafka.topics.orders-processed}") String ordersProcessedTopic,
                                   @Value("${kafka.topics.orders-to-process-dlq}") String ordersDlqTopic,
                                   OrderCalculationService orderCalculationService) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersProcessedTopic = ordersProcessedTopic;
        this.ordersDlqTopic = ordersDlqTopic;
        this.orderCalculationService = orderCalculationService;
    }

    @KafkaListener(topics = "${kafka.topics.orders-to-process}", groupId = "order-processing-group")
    public void processOrder(
            @Payload(required = false) OrderProcessingEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            if (event == null) {
                throw new IllegalArgumentException("Received null event");
            }

            BigDecimal totalAmount = orderCalculationService.calculateTotalAmount(event);

            OrderProcessedEvent processedEvent = OrderProcessedEvent.builder()
                    .orderId(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .totalAmount(totalAmount)
                    .items(event.getItems())
                    .build();

            kafkaTemplate.send(ordersProcessedTopic, key, processedEvent);
            log.info("Successfully published to topic {} with key {}", ordersProcessedTopic, key);

        } catch (Exception e) {
            log.error("Error processing order event - Topic: {}, Key: {}, Offset: {}, Error: {}",
                    topic, key, offset, e.getMessage());
            publishToDlq(event, key, topic, offset, e);
        }
    }

    private void publishToDlq(OrderProcessingEvent event, String key, String originalTopic, long offset, Exception exception) {
        try {
            DlqMessage dlqMessage = DlqMessage.builder()
                    .originalEvent(event)
                    .originalTopic(originalTopic)
                    .originalOffset(offset)
                    .errorMessage(exception.getMessage())
                    .build();
            kafkaTemplate.send(ordersDlqTopic, key, dlqMessage);
            log.info("Published to DLQ topic {} with key {}", ordersDlqTopic, key);
        } catch (Exception ex) {
            log.error("Failed to publish to DLQ topic {}: {}", ordersDlqTopic, ex.getMessage());
        }
    }
}