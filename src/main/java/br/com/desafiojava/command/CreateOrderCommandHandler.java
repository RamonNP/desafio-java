package br.com.desafiojava.command;

import br.com.desafiojava.common.exception.OrderProcessingException;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.event.OrderEventPublisher;
import br.com.desafiojava.event.OrderItem;
import br.com.desafiojava.event.OrderProcessingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long IDEMPOTENCY_TTL = 86400; // 24 horas em segundos

    public CreateOrderCommandHandler(OrderEventPublisher eventPublisher,
                                     RedisTemplate<String, String> redisTemplate) {
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void handle(CreateOrderCommand command) {
        String orderId = command.getOrderId();
        try {
            validateCommand(command);

            String startDate = LocalDateTime.now().toString();
            Boolean keyExists = redisTemplate.opsForValue().setIfAbsent(orderId, startDate, IDEMPOTENCY_TTL, TimeUnit.SECONDS);
            if (keyExists == null || !keyExists) {
                log.warn("Order {} is already being processed", command.getOrderId());
                throw new IllegalStateException("Request already being processed: " + command.getOrderId());
            }

            List<OrderItem> items = command.getItems().stream()
                    .map(item -> OrderItem.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .build())
                    .toList();


            OrderProcessingEvent event = OrderProcessingEvent.builder()
                    .orderId(orderId)
                    .customerId(command.getCustomerId())
                    .items(items)
                    .build();

            publishEvent(event, command.getOrderId(), orderId);

        } catch (IllegalArgumentException e) {
            log.error("Validation error for order {}: {}", command.getOrderId(), e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            log.warn("Idempotency check failed for order {}: {}", command.getOrderId(), e.getMessage());
            throw e;
        } catch (OrderProcessingException e) {
            log.error("Processing error for order {}: {}", command.getOrderId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing order {}: {}", command.getOrderId(), e.getMessage(), e);
            cleanupRedisKey(orderId);
            throw new OrderProcessingException("Failed to process order: " + e.getMessage(), e);
        }
    }

    private void validateCommand(CreateOrderCommand command) {
        if (command.getOrderId() == null || command.getOrderId().isBlank()) {
            log.error("Order ID is empty");
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (command.getCustomerId() == null || command.getCustomerId().isBlank()) {
            log.error("Customer ID is empty");
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            log.error("Order items are empty");
            throw new IllegalArgumentException("Order must have at least one item");
        }

        for (CreateOrderCommand.OrderItem item : command.getItems()) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                log.error("Product ID is empty for item");
                throw new IllegalArgumentException("Product ID cannot be empty");
            }
            if (item.getQuantity() <= 0) {
                log.error("Invalid quantity {} for item", item.getQuantity());
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Invalid price {} for item", item.getPrice());
                throw new IllegalArgumentException("Price must be greater than zero");
            }
        }
    }

    private void publishEvent(OrderProcessingEvent event, String orderIdForLog, String redisKey) {
        try {
            eventPublisher.publishProcessingEvent(event);
            log.info("Published processing event for order {}", orderIdForLog);
        } catch (Exception e) {
            log.error("Failed to publish event for order {}: {}", orderIdForLog, e.getMessage());
            cleanupRedisKey(redisKey);
            throw new OrderProcessingException("Failed to publish order event: " + e.getMessage(), e);
        }
    }

    private void cleanupRedisKey(String redisKey) {
        try {
            redisTemplate.delete(redisKey);
        } catch (Exception redisEx) {
            log.error("Failed to clean up Redis key {}: {}", redisKey, redisEx.getMessage());
        }
    }
}