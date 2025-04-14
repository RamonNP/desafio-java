package br.com.desafiojava.common.exception;

public class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;

    public FieldError() {}

    public FieldError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    // Getters e Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String field;
        private String message;
        private Object rejectedValue;

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder rejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        public FieldError build() {
            return new FieldError(field, message, rejectedValue);
        }
    }
}
