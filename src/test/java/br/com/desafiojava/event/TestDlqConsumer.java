package br.com.desafiojava.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TestDlqConsumer {

    private final List<DlqMessage> messages = new ArrayList<>();

    @KafkaListener(topics = "${kafka.topics.orders-dlq}", groupId = "test-dlq-group")
    public void consumeDlq(DlqMessage dlqMessage) {
        log.info("Received in test DLQ consumer: {}", dlqMessage);
        synchronized (messages) {
            messages.add(dlqMessage);
        }
    }

    public List<DlqMessage> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    public void clearMessages() {
        synchronized (messages) {
            messages.clear();
        }
    }
}