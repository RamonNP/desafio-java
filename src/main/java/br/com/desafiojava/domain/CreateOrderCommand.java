package br.com.desafiojava.domain;

import java.math.BigDecimal;

public class CreateOrderCommand {
    private final String customerId;
    private final BigDecimal totalAmount;
    private final String idempotencyKey;

    public CreateOrderCommand(String customerId, BigDecimal totalAmount, String idempotencyKey) {
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.idempotencyKey = idempotencyKey;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
