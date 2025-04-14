package br.com.desafiojava.api.dto;

import java.math.BigDecimal;

public class CreateOrderRequestDto {
    private String customerId;
    private BigDecimal totalAmount;
    private String idempotencyKey;

    // Construtor vazio para deserialização JSON
    public CreateOrderRequestDto() {}

    // Construtor completo
    public CreateOrderRequestDto(String customerId, BigDecimal totalAmount, String idempotencyKey) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
    }

    // Getters e Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}