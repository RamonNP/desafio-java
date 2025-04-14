package br.com.desafiojava.event;

import java.util.List;

public class OrderProcessingEvent {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;

    private OrderProcessingEvent(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.items = builder.items;
        validate();
    }

    // No-args constructor for frameworks (e.g., Jackson)
    public OrderProcessingEvent() {
        this.orderId = null;
        this.customerId = null;
        this.items = null;
    }

    private void validate() {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Items list cannot be null or empty");
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
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
        private List<OrderItem> items;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public OrderProcessingEvent build() {
            return new OrderProcessingEvent(this);
        }
    }
}