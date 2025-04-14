package br.com.desafiojava.domain;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderCommand {
    private final String customerId;
    private final List<OrderItem> items;
    private final String orderId;


    private CreateOrderCommand(Builder builder) {
        this.customerId = builder.customerId;
        this.items = builder.items;
        this.orderId = builder.orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public String getOrderId() {
        return orderId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String customerId;
        private List<OrderItem> items;
        private String orderId;

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public CreateOrderCommand build() {
            return new CreateOrderCommand(this);
        }
    }

    public static class OrderItem {
        private final String productId;
        private final int quantity;
        private final BigDecimal price;

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
}
