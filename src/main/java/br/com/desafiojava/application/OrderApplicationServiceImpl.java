package br.com.desafiojava.application;

import br.com.desafiojava.command.CreateOrderCommandHandler;
import br.com.desafiojava.common.exception.OrderProcessingException;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.query.FindOrderByIdQuery;
import br.com.desafiojava.query.FindOrderByIdQueryHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final CreateOrderCommandHandler createHandler;
    private final FindOrderByIdQueryHandler findHandler;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper; // Para serialização/desserialização

    public OrderApplicationServiceImpl(CreateOrderCommandHandler createHandler,
                                       FindOrderByIdQueryHandler findHandler,
                                       RedisTemplate<String, String> redisTemplate,
                                       ObjectMapper objectMapper) {
        this.createHandler = createHandler;
        this.findHandler = findHandler;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void createOrder(CreateOrderCommand command) {
        createHandler.handle(command);
    }

    @Override
    public Optional<OrderDto> findOrderById(String orderId) {
        try {
            String cacheKey = "order:" + orderId;
            String cachedOrderJson = redisTemplate.opsForValue().get(cacheKey);
            if (cachedOrderJson != null) {
                OrderDto cachedOrder = objectMapper.readValue(cachedOrderJson, OrderDto.class);
                return Optional.of(cachedOrder);
            }
            Optional<OrderDto> order = findHandler.handle(new FindOrderByIdQuery(orderId));
            order.ifPresent(o -> {
                try {
                    String orderJson = objectMapper.writeValueAsString(o);
                    redisTemplate.opsForValue().set(cacheKey, orderJson, 10, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.error("Failed to serialize OrderDto to JSON for caching: {}", e.getMessage(), e);
                }
            });
            return order;
        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving order {}: {}", orderId, e.getMessage(), e);
            throw new OrderProcessingException("Failed to retrieve order: " + e.getMessage());
        }
    }
}