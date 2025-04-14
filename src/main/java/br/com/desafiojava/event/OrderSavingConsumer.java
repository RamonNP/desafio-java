package br.com.desafiojava.event;

import br.com.desafiojava.common.exception.KafkaProcessingException;
import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderItemEntity;
import br.com.desafiojava.domain.OrderStatus;
import br.com.desafiojava.repository.OrderWriteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderSavingConsumer {
    private final OrderWriteRepository repository;

    public OrderSavingConsumer(OrderWriteRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${kafka.topics.orders-processed}", groupId = "order-saving-group")
    public void saveOrder(OrderProcessedEvent event) {
        try {
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

        } catch (KafkaProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new KafkaProcessingException("Unexpected error saving order: " + e.getMessage());
        }
    }
}