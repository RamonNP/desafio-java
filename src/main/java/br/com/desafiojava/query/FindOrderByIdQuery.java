package br.com.desafiojava.query;

import java.util.UUID;

public class FindOrderByIdQuery {
    private final UUID orderId;

    public FindOrderByIdQuery(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}
