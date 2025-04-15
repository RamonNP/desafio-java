package br.com.desafiojava.event;

import lombok.Builder;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Data
@Builder
public class DlqMessage<T> {
    private T originalEvent;
    private String originalTopic;
    private long originalOffset;
    private String errorMessage;
    private String stackTrace;
    private LocalDateTime errorTimestamp;
    private String consumerGroup;

    public static <T> DlqMessage<T> create(T event, String topic, long offset,
                                           Exception exception, String consumerGroup) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));

        return DlqMessage.<T>builder()
                .originalEvent(event)
                .originalTopic(topic)
                .originalOffset(offset)
                .errorMessage(exception.getMessage())
                .stackTrace(sw.toString())
                .errorTimestamp(LocalDateTime.now())
                .consumerGroup(consumerGroup)
                .build();
    }
}