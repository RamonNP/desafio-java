package br.com.desafiojava.event;

import java.math.BigDecimal;

public class OrderItem {
    private String productId;
    private int quantity;
    private BigDecimal price;

    public OrderItem() {
    }

    public OrderItem(String productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
