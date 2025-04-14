package br.com.desafiojava.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {
    private String id;
    private String customerId;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private List<OrderItemDto> items; // Adicionando os itens aqui

    public OrderDto(String id, String customerId, BigDecimal totalAmount, LocalDateTime createdAt, OrderStatus status, List<OrderItemDto> items) {
        this.id = id;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.status = status;
        this.items = items;
    }

    // Getters
    public String getId() {
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

    public List<OrderItemDto> getItems() {
        return items;
    }
}
