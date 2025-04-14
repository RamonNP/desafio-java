package br.com.desafiojava.event;

import java.math.BigDecimal;

public class OrderItem {
    private String productId;
    private int quantity;
    private BigDecimal price;

    public OrderItem() {
    }

    private OrderItem(Builder builder) {
        this.productId = builder.productId;
        this.quantity = builder.quantity;
        this.price = builder.price;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String productId;
        private int quantity;
        private BigDecimal price;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}
