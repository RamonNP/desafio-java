package br.com.desafiojava.event;

import java.math.BigDecimal;
import java.util.List;

public class OrderProcessedEvent {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private List<OrderItem> items;

    public OrderProcessedEvent() {
    }

    private OrderProcessedEvent(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.totalAmount = builder.totalAmount;
        this.items = builder.items;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String orderId;
        private String customerId;
        private BigDecimal totalAmount;
        private List<OrderItem> items;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public OrderProcessedEvent build() {
            return new OrderProcessedEvent(this);
        }
    }
}
