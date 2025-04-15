package br.com.desafiojava.event;

import br.com.desafiojava.common.exception.KafkaProcessingException;
import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderItemEntity;
import br.com.desafiojava.domain.OrderStatus;
import br.com.desafiojava.repository.OrderWriteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderSavingConsumer {
    private final OrderWriteRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersDlqTopic;

    public OrderSavingConsumer(OrderWriteRepository repository,
                               KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${kafka.topics.orders-processed-dlq}") String ordersDlqTopic) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersDlqTopic = ordersDlqTopic;
    }

    @KafkaListener(topics = "${kafka.topics.orders-processed}", groupId = "order-saving-group")
    public void saveOrder(
            @Payload(required = false) OrderProcessedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            if (event == null) {
                throw new IllegalArgumentException("Received null event");
            }

            List<OrderItemEntity> itemEntities = event.getItems().stream()
                    .map(item -> OrderItemEntity.builder()
                            .orderId(event.getOrderId())
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();

            Order order = Order.builder()
                    .id(event.getOrderId())
                    .customerId(event.getCustomerId())
                    .totalAmount(event.getTotalAmount())
                    .createdAt(LocalDateTime.now())
                    .status(OrderStatus.PROCESSED)
                    .items(itemEntities)
                    .build();

            repository.save(order);
            log.info("Successfully saved order {}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error saving order - Topic: {}, Key: {}, Offset: {}, Error: {}",
                    topic, key, offset, e.getMessage());
            publishToDlq(event, key, topic, offset, e);
        }
    }

    private void publishToDlq(OrderProcessedEvent event, String key, String originalTopic, long offset, Exception exception) {
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