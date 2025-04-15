package br.com.desafiojava.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class TestDlqConsumer {

    private final List<DlqMessage> messages = new CopyOnWriteArrayList<>();

    @KafkaListener(topics = "${kafka.topics.orders-to-process-dlq}", groupId = "test-dlq-group")
    public void consumeDlq(DlqMessage dlqMessage) {
        log.info("Received in test DLQ consumer: {}", dlqMessage);
        messages.add(dlqMessage);
    }

    public List<DlqMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessages() {
        messages.clear();
    }
}