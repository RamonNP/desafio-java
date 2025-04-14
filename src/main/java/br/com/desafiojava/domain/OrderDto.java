package br.com.desafiojava.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDto {
    private UUID id;
    private String customerId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private OrderStatus status;

    public OrderDto(UUID id, String customerId, BigDecimal totalAmount, LocalDateTime createdAt, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Getters
    public UUID getId() {
        return id;
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

    public OrderStatus getStatus() {
        return status;
    }
}
