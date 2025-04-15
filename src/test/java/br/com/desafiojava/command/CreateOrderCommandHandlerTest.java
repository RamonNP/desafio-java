package br.com.desafiojava.command;

import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.event.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateOrderCommandHandlerTest {

    @Mock
    private OrderEventPublisher eventPublisher;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CreateOrderCommandHandler handler;

    private CreateOrderCommand createValidCommand() {
        return CreateOrderCommand.builder()
                .orderId("123")
                .customerId("cust-1")
                .items(List.of(CreateOrderCommand.OrderItem.builder()
                        .productId("prod-1")
                        .quantity(1)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();
    }

    @Test
    public void shouldThrowExceptionWhenOrderIdIsEmpty() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId("")
                .customerId("cust-1")
                .items(List.of(CreateOrderCommand.OrderItem.builder()
                        .productId("prod-1")
                        .quantity(1)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowExceptionWhenCustomerIdIsEmpty() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId("123")
                .customerId("")
                .items(List.of(CreateOrderCommand.OrderItem.builder()
                        .productId("prod-1")
                        .quantity(1)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowExceptionWhenItemsAreEmpty() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId("123")
                .customerId("cust-1")
                .items(List.of())
                .build();
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldPublishEventWhenCommandIsValid() {
        // Arrange
        String orderId = "123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(orderId), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);

        CreateOrderCommand command = createValidCommand();

        // Act
        handler.handle(command);

        // Assert
        verify(eventPublisher, times(1)).publishProcessingEvent(any());
    }

    @Test
    public void shouldNotProcessDuplicateRequest() {
        // Arrange
        String orderId = "123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(orderId), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        CreateOrderCommand command = createValidCommand();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> handler.handle(command));
        verify(eventPublisher, never()).publishProcessingEvent(any());
    }

    @Test
    public void shouldCleanupRedisKeyWhenPublishFails() {
        // Arrange
        String orderId = "123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(orderId), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        doThrow(new RuntimeException("Publish failed")).when(eventPublisher).publishProcessingEvent(any());

        CreateOrderCommand command = createValidCommand();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> handler.handle(command));
        verify(redisTemplate, times(1)).delete(orderId);
    }
}