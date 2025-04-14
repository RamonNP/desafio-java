package br.com.desafiojava.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Validation error in request fields")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument error: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        log.warn("Illegal state error: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(KafkaProcessingException.class)
    public ResponseEntity<ApiError> handleKafkaProcessingException(KafkaProcessingException ex, WebRequest request) {
        log.error("Kafka processing error: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Message Processing Error")
                .message(ex.getMessage())
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}