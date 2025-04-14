package br.com.desafiojava.command;

import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderStatus;
import br.com.desafiojava.event.OrderCreatedEvent;
import br.com.desafiojava.event.OrderEventPublisher;
import br.com.desafiojava.repository.OrderWriteRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand> {

    private final OrderWriteRepository repository;
    private final OrderEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;
    private static final long IDEMPOTENCY_TTL = 24 * 60 * 60; // 24 horas em segundos

    public CreateOrderCommandHandler(OrderWriteRepository repository,
                                     OrderEventPublisher eventPublisher,
                                     RedisTemplate<String, String> redisTemplate) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void handle(CreateOrderCommand command) {
        String idempotencyKey = "order:" + command.getIdempotencyKey();

        Boolean keyExists = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "processed", IDEMPOTENCY_TTL, TimeUnit.SECONDS);
        if (keyExists == null || !keyExists) {
            throw new IllegalStateException("Request already processed: " + command.getIdempotencyKey());
        }

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .customerId(command.getCustomerId())
                .totalAmount(command.getTotalAmount())
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .build();

        repository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
        eventPublisher.publish(event);
    }
}
