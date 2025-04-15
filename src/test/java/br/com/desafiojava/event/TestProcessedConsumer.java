package br.com.desafiojava.event;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Component
public class TestProcessedConsumer {

    private final List<ConsumerRecord<String, OrderProcessedEvent>> messages = new ArrayList<>();
    private CountDownLatch latch = new CountDownLatch(1);

    @KafkaListener(topics = "${kafka.topics.orders-processed}", groupId = "test-group")
    public void consume(ConsumerRecord<String, OrderProcessedEvent> record) {
        messages.add(record);
        latch.countDown();
    }

    public List<ConsumerRecord<String, OrderProcessedEvent>> getMessages() {
        return messages;
    }

    public void reset() {
        messages.clear();
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}