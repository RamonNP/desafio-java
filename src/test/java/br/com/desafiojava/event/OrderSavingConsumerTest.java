package br.com.desafiojava.event;

import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderItemEntity;
import br.com.desafiojava.domain.OrderStatus;
import br.com.desafiojava.repository.OrderWriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderSavingConsumerTest {

    @Mock
    private OrderWriteRepository repository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderSavingConsumer orderSavingConsumer;

    private final String ordersDlqTopic = "orders-processed-dlq";
    private final String key = "test-key";
    private final String topic = "orders-processed";
    private final long offset = 1L;

    @BeforeEach
    void setUp() {
        orderSavingConsumer = new OrderSavingConsumer(repository, kafkaTemplate, ordersDlqTopic);
    }

    @Test
    void shouldSaveValidOrderSuccessfully() {
        // Arrange
        OrderProcessedEvent event = createValidOrderProcessedEvent();
        when(repository.save(any(Order.class))).thenReturn(any(Order.class));

        // Act
        orderSavingConsumer.saveOrder(event, key, topic, offset);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertEquals(event.getOrderId(), capturedOrder.getId());
        assertEquals(event.getCustomerId(), capturedOrder.getCustomerId());
        assertEquals(event.getTotalAmount(), capturedOrder.getTotalAmount());
        assertEquals(OrderStatus.PROCESSED, capturedOrder.getStatus());
        assertEquals(1, capturedOrder.getItems().size());
        assertEquals(event.getItems().get(0).getProductId(), capturedOrder.getItems().get(0).getProductId());
        assertEquals(event.getItems().get(0).getQuantity(), capturedOrder.getItems().get(0).getQuantity());
        assertEquals(event.getItems().get(0).getPrice(), capturedOrder.getItems().get(0).getPrice());

        verify(kafkaTemplate, never()).send(eq(ordersDlqTopic), eq(key), any());
    }

    @Test
    void shouldSendToDlqWhenEventIsNull() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(eq(ordersDlqTopic), eq(key), any(DlqMessage.class))).thenReturn(future);

        // Act
        orderSavingConsumer.saveOrder(null, key, topic, offset);

        // Assert
        ArgumentCaptor<DlqMessage> dlqCaptor = ArgumentCaptor.forClass(DlqMessage.class);
        verify(kafkaTemplate).send(eq(ordersDlqTopic), eq(key), dlqCaptor.capture());

        DlqMessage capturedDlq = dlqCaptor.getValue();
        assertNull(capturedDlq.getOriginalEvent());
        assertEquals("Received null event", capturedDlq.getErrorMessage());
        assertEquals(topic, capturedDlq.getOriginalTopic());
        assertEquals(offset, capturedDlq.getOriginalOffset());

        verify(repository, never()).save(any());
    }

    @Test
    void shouldSendToDlqWhenSavingOrderFailsWithException() {
        // Arrange
        OrderProcessedEvent event = createValidOrderProcessedEvent();
        when(repository.save(any(Order.class))).thenThrow(new RuntimeException("Database error"));
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(eq(ordersDlqTopic), eq(key), any(DlqMessage.class))).thenReturn(future);

        // Act
        orderSavingConsumer.saveOrder(event, key, topic, offset);

        // Assert
        ArgumentCaptor<DlqMessage> dlqCaptor = ArgumentCaptor.forClass(DlqMessage.class);
        verify(kafkaTemplate).send(eq(ordersDlqTopic), eq(key), dlqCaptor.capture());

        DlqMessage capturedDlq = dlqCaptor.getValue();
        assertEquals(event, capturedDlq.getOriginalEvent());
        assertTrue(capturedDlq.getErrorMessage().contains("Database error"));
        assertEquals(topic, capturedDlq.getOriginalTopic());
        assertEquals(offset, capturedDlq.getOriginalOffset());

        verify(repository).save(any(Order.class));
    }

    private OrderProcessedEvent createValidOrderProcessedEvent() {
        return OrderProcessedEvent.builder()
                .orderId("order-123")
                .customerId("cust-1")
                .totalAmount(new BigDecimal("20.00"))
                .items(List.of(OrderItem.builder()
                        .productId("prod-1")
                        .quantity(2)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();
    }
}