package br.com.desafiojava.application;

import br.com.desafiojava.command.CreateOrderCommandHandler;
import br.com.desafiojava.common.exception.OrderProcessingException;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.query.FindOrderByIdQuery;
import br.com.desafiojava.query.FindOrderByIdQueryHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderApplicationServiceImplTest {

    @Mock
    private CreateOrderCommandHandler createHandler;

    @Mock
    private FindOrderByIdQueryHandler findHandler;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OrderApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        // Configuração lenient para evitar UnnecessaryStubbingException
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId("123")
                .customerId("cust-1")
                .build();

        service.createOrder(command);

        verify(createHandler, times(1)).handle(command);
        verifyNoMoreInteractions(createHandler);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCreateOrderFails() {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .orderId("123")
                .customerId("cust-1")
                .build();

        doThrow(new IllegalArgumentException("Invalid order")).when(createHandler).handle(command);

        assertThrows(IllegalArgumentException.class, () -> service.createOrder(command));
        verify(createHandler, times(1)).handle(command);
        verifyNoMoreInteractions(createHandler);
    }

    @Test
    void shouldFindOrderByIdFromCache() throws Exception {
        String orderId = "123";
        String cacheKey = "order:" + orderId;
        OrderDto orderDto = new OrderDto(orderId, "cust-1", null, null, null, null);
        String orderJson = "{\"id\":\"123\",\"customerId\":\"cust-1\"}";

        when(valueOperations.get(cacheKey)).thenReturn(orderJson);
        when(objectMapper.readValue(orderJson, OrderDto.class)).thenReturn(orderDto);

        Optional<OrderDto> result = service.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        assertEquals("cust-1", result.get().getCustomerId());
        verify(valueOperations, times(1)).get(cacheKey);
        verify(objectMapper, times(1)).readValue(orderJson, OrderDto.class);
        verify(findHandler, never()).handle(any());
    }

    @Test
    void shouldFindOrderByIdFromDatabaseAndCacheIt() throws Exception {
        String orderId = "456";
        String cacheKey = "order:" + orderId;
        OrderDto orderDto = new OrderDto(orderId, "cust-2", null, null, null, null);
        String orderJson = "{\"id\":\"456\",\"customerId\":\"cust-2\"}";

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(findHandler.handle(argThat(query -> query.getOrderId().equals(orderId))))
                .thenReturn(Optional.of(orderDto));
        when(objectMapper.writeValueAsString(orderDto)).thenReturn(orderJson);

        Optional<OrderDto> result = service.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        assertEquals("cust-2", result.get().getCustomerId());
        verify(valueOperations, times(1)).get(cacheKey);
        verify(findHandler, times(1)).handle(any(FindOrderByIdQuery.class));
        verify(objectMapper, times(1)).writeValueAsString(orderDto);
        verify(valueOperations, times(1)).set(eq(cacheKey), eq(orderJson), eq(10L), any());
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        String orderId = "999";
        String cacheKey = "order:" + orderId;

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(findHandler.handle(argThat(query -> query.getOrderId().equals(orderId))))
                .thenReturn(Optional.empty());

        Optional<OrderDto> result = service.findOrderById(orderId);

        assertFalse(result.isPresent());
        verify(valueOperations, times(1)).get(cacheKey);
        verify(findHandler, times(1)).handle(any(FindOrderByIdQuery.class));
        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowOrderProcessingExceptionWhenCacheDeserializationFails() throws JsonProcessingException {
        String orderId = "123";
        String cacheKey = "order:" + orderId;
        String invalidJson = "{invalid}";

        when(valueOperations.get(cacheKey)).thenReturn(invalidJson);
        when(objectMapper.readValue(invalidJson, OrderDto.class))
                .thenThrow(new RuntimeException("Deserialization error"));

        assertThrows(OrderProcessingException.class, () -> service.findOrderById(orderId));
        verify(valueOperations, times(1)).get(cacheKey);
        verify(objectMapper, times(1)).readValue(invalidJson, OrderDto.class);
        verify(findHandler, never()).handle(any());
    }

    @Test
    void shouldReturnOrderWhenCacheSerializationFails() throws Exception {
        String orderId = "456";
        String cacheKey = "order:" + orderId;
        OrderDto orderDto = new OrderDto(orderId, "cust-2", null, null, null, null);

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(findHandler.handle(argThat(query -> query.getOrderId().equals(orderId))))
                .thenReturn(Optional.of(orderDto));
        when(objectMapper.writeValueAsString(orderDto)).thenThrow(new RuntimeException("Serialization error"));

        Optional<OrderDto> result = service.findOrderById(orderId);

        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        assertEquals("cust-2", result.get().getCustomerId());
        verify(valueOperations, times(1)).get(cacheKey);
        verify(findHandler, times(1)).handle(any(FindOrderByIdQuery.class));
        verify(objectMapper, times(1)).writeValueAsString(orderDto);
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }
}