package br.com.desafiojava.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ApiError {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    public ApiError() {}

    public ApiError(LocalDateTime timestamp, int status, String error, String message, String path, List<FieldError> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    // Getters e Setters

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<FieldError> fieldErrors;

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ApiError build() {
            return new ApiError(timestamp, status, error, message, path, fieldErrors);
        }
    }
}
