package br.com.desafiojava.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderCreatedEvent {
    private final String orderId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;

    public OrderCreatedEvent(String orderId, String customerId, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
