package br.com.desafiojava.event;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DlqMessageTest {

    @Test
    void shouldCreateDlqMessageWithAllFields() {
        // Given
        Exception exception = new RuntimeException("Test error");
        String topic = "test-topic";
        long offset = 123L;
        String consumerGroup = "test-group";
        OrderProcessingEvent event = OrderProcessingEvent.builder()
                .orderId("test-order")
                .build();

        // When
        DlqMessage<OrderProcessingEvent> dlqMessage = DlqMessage.create(
                event, topic, offset, exception, consumerGroup);

        // Then
        assertNotNull(dlqMessage);
        assertEquals(event, dlqMessage.getOriginalEvent());
        assertEquals(topic, dlqMessage.getOriginalTopic());
        assertEquals(offset, dlqMessage.getOriginalOffset());
        assertEquals("Test error", dlqMessage.getErrorMessage());
        assertTrue(dlqMessage.getStackTrace().contains("RuntimeException"));
        assertNotNull(dlqMessage.getErrorTimestamp());
        assertEquals(consumerGroup, dlqMessage.getConsumerGroup());
    }

    @Test
    void shouldHandleNullEvent() {
        // Given
        Exception exception = new RuntimeException("Null event test");
        String topic = "test-topic";
        long offset = 456L;
        String consumerGroup = "test-group";

        // When
        DlqMessage<OrderProcessingEvent> dlqMessage = DlqMessage.create(
                null, topic, offset, exception, consumerGroup);

        // Then
        assertNull(dlqMessage.getOriginalEvent());
        assertEquals(topic, dlqMessage.getOriginalTopic());
        assertEquals(offset, dlqMessage.getOriginalOffset());
    }

    @Test
    void builderShouldCreateCompleteObject() {
        // Given
        OrderProcessingEvent event = OrderProcessingEvent.builder()
                .orderId("builder-test")
                .build();
        LocalDateTime now = LocalDateTime.now();

        // When
        DlqMessage<OrderProcessingEvent> dlqMessage = DlqMessage.<OrderProcessingEvent>builder()
                .originalEvent(event)
                .originalTopic("builder-topic")
                .originalOffset(789L)
                .errorMessage("builder error")
                .stackTrace("stack trace")
                .errorTimestamp(now)
                .consumerGroup("builder-group")
                .build();

        // Then
        assertEquals(event, dlqMessage.getOriginalEvent());
        assertEquals("builder-topic", dlqMessage.getOriginalTopic());
        assertEquals(789L, dlqMessage.getOriginalOffset());
        assertEquals("builder error", dlqMessage.getErrorMessage());
        assertEquals("stack trace", dlqMessage.getStackTrace());
        assertEquals(now, dlqMessage.getErrorTimestamp());
        assertEquals("builder-group", dlqMessage.getConsumerGroup());
    }
}