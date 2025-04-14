package br.com.desafiojava.event;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderProcessedEvent {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;

    public OrderProcessedEvent() {
    }

    public OrderProcessedEvent(String orderId, String customerId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
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

}