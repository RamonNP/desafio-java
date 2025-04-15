package br.com.desafiojava.event;

import br.com.desafiojava.application.OrderCalculationService;
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
public class OrderProcessingConsumerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private OrderCalculationService orderCalculationService;

    @InjectMocks
    private OrderProcessingConsumer orderProcessingConsumer;

    private final String ordersProcessedTopic = "orders-processed";
    private final String ordersDlqTopic = "orders-to-process-dlq";
    private final String key = "test-key";
    private final String topic = "orders-to-process";
    private final long offset = 1L;

    @BeforeEach
    void setUp() {
        orderProcessingConsumer = new OrderProcessingConsumer(kafkaTemplate, ordersProcessedTopic, ordersDlqTopic, orderCalculationService);
    }

    @Test
    void shouldProcessValidOrderAndSendToProcessedTopic() {
        // Arrange
        OrderProcessingEvent event = createValidOrderProcessingEvent();
        when(orderCalculationService.calculateTotalAmount(event)).thenReturn(new BigDecimal("20"));

        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(eq(ordersProcessedTopic), eq(key), any(OrderProcessedEvent.class))).thenReturn(future);

        // Act
        orderProcessingConsumer.processOrder(event, key, topic, offset);

        // Assert
        verify(orderCalculationService).calculateTotalAmount(event);

        ArgumentCaptor<OrderProcessedEvent> processedEventCaptor = ArgumentCaptor.forClass(OrderProcessedEvent.class);
        verify(kafkaTemplate).send(eq(ordersProcessedTopic), eq(key), processedEventCaptor.capture());

        OrderProcessedEvent capturedEvent = processedEventCaptor.getValue();
        assertEquals(event.getOrderId(), capturedEvent.getOrderId());
        assertEquals(event.getCustomerId(), capturedEvent.getCustomerId());
        assertEquals(new BigDecimal("20"), capturedEvent.getTotalAmount());
        assertEquals(1, capturedEvent.getItems().size());

        verify(kafkaTemplate, never()).send(eq(ordersDlqTopic), eq(key), any());
    }

    @Test
    void shouldSendToDlqWhenEventIsNull() {
        // Arrange
        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(eq(ordersDlqTopic), eq(key), any(DlqMessage.class))).thenReturn(future);

        // Act
        orderProcessingConsumer.processOrder(null, key, topic, offset);

        // Assert
        ArgumentCaptor<DlqMessage> dlqCaptor = ArgumentCaptor.forClass(DlqMessage.class);
        verify(kafkaTemplate).send(eq(ordersDlqTopic), eq(key), dlqCaptor.capture());

        DlqMessage capturedDlq = dlqCaptor.getValue();
        assertNull(capturedDlq.getOriginalEvent());
        assertEquals("Received null event", capturedDlq.getErrorMessage());
        assertEquals(topic, capturedDlq.getOriginalTopic());
        assertEquals(offset, capturedDlq.getOriginalOffset());

        verify(orderCalculationService, never()).calculateTotalAmount(any());
        verify(kafkaTemplate, never()).send(eq(ordersProcessedTopic), eq(key), any());
    }

    @Test
    void shouldSendToDlqWhenProcessingFailsWithException() {
        // Arrange
        OrderProcessingEvent event = createValidOrderProcessingEvent();
        when(orderCalculationService.calculateTotalAmount(event)).thenThrow(new RuntimeException("Calculation failed"));

        CompletableFuture<SendResult<String, Object>> future = mock(CompletableFuture.class);
        when(kafkaTemplate.send(eq(ordersDlqTopic), eq(key), any(DlqMessage.class))).thenReturn(future);

        // Act
        orderProcessingConsumer.processOrder(event, key, topic, offset);

        // Assert
        ArgumentCaptor<DlqMessage> dlqCaptor = ArgumentCaptor.forClass(DlqMessage.class);
        verify(kafkaTemplate).send(eq(ordersDlqTopic), eq(key), dlqCaptor.capture());

        DlqMessage capturedDlq = dlqCaptor.getValue();
        assertEquals(event, capturedDlq.getOriginalEvent());
        assertTrue(capturedDlq.getErrorMessage().contains("Calculation failed"));
        assertEquals(topic, capturedDlq.getOriginalTopic());
        assertEquals(offset, capturedDlq.getOriginalOffset());

        verify(orderCalculationService).calculateTotalAmount(event);
        verify(kafkaTemplate, never()).send(eq(ordersProcessedTopic), eq(key), any(OrderProcessedEvent.class));
    }

    private OrderProcessingEvent createValidOrderProcessingEvent() {
        return OrderProcessingEvent.builder()
                .orderId("order-123")
                .customerId("cust-1")
                .items(List.of(OrderItem.builder()
                        .productId("prod-1")
                        .quantity(2)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();
    }
}
