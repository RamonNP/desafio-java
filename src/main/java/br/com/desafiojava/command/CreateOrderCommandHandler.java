package br.com.desafiojava.command;

import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.event.OrderEventPublisher;
import br.com.desafiojava.event.OrderItem;
import br.com.desafiojava.event.OrderProcessingEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long IDEMPOTENCY_TTL = 24 * 60 * 60; // 24 horas em segundos

    public CreateOrderCommandHandler(OrderEventPublisher eventPublisher, RedisTemplate<String, String> redisTemplate) {
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void handle(CreateOrderCommand command) {
        String orderId = "order:" + command.getOrderId();

        Boolean keyExists = redisTemplate.opsForValue().setIfAbsent(orderId, "processing", IDEMPOTENCY_TTL, TimeUnit.SECONDS);
        if (keyExists == null || !keyExists) {
            throw new IllegalStateException("Request already being processed: " + command.getOrderId());
        }

        // Validação da entrada
        if (command.getCustomerId() == null || command.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        for (CreateOrderCommand.OrderItem item : command.getItems()) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                throw new IllegalArgumentException("Product ID cannot be empty");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Price must be greater than zero");
            }
        }

        List<OrderItem> items = command.getItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getPrice()))
                .collect(Collectors.toList());

        OrderProcessingEvent event = new OrderProcessingEvent(orderId, command.getCustomerId(), items);
        eventPublisher.publishProcessingEvent(event);
    }
}