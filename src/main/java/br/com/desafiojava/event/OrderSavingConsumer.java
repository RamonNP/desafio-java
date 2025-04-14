package br.com.desafiojava.event;

import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderStatus;
import br.com.desafiojava.repository.OrderWriteRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderSavingConsumer {

    private final OrderWriteRepository repository;

    public OrderSavingConsumer(OrderWriteRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${kafka.topics.orders-processed}", groupId = "order-saving-group")
    public void saveOrder(OrderProcessedEvent event) {
        Order order = Order.builder()
                .id(event.getOrderId())
                .customerId(event.getCustomerId())
                .totalAmount(event.getTotalAmount())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PROCESSED)
                .build();

        repository.save(order);
    }
}