package br.com.desafiojava.domain;

import java.time.LocalDateTime;

public class OrderStatusDto {

    private String orderId;
    private String status;
    private LocalDateTime startDate;

    public OrderStatusDto() {
    }

    public OrderStatusDto(String orderId, String status, LocalDateTime startDate) {
        this.orderId = orderId;
        this.status = status;
        this.startDate = startDate;
    }

    private OrderStatusDto(Builder builder) {
        this.orderId = builder.orderId;
        this.status = builder.status;
        this.startDate = builder.startDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderId;
        private String status;
        private LocalDateTime startDate;

        private Builder() {

        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public OrderStatusDto build() {
            return new OrderStatusDto(this);
        }
    }
}