package br.com.desafiojava.common.exception;

public class KafkaProcessingException extends RuntimeException {
    public KafkaProcessingException(String message) {
        super(message);
    }
}
