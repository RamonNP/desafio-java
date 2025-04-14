package br.com.desafiojava.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String ordersToProcessTopic;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${kafka.topics.orders-to-process}") String ordersToProcessTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersToProcessTopic = ordersToProcessTopic;
    }

    public void publishProcessingEvent(OrderProcessingEvent event) {
        kafkaTemplate.send(ordersToProcessTopic, event.getOrderId().toString(), event);
    }
}