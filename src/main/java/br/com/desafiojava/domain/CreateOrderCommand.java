package br.com.desafiojava.domain;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderCommand {
    private final String customerId;
    private final List<OrderItem> items;
    private final String orderId;

    public CreateOrderCommand(String customerId, List<OrderItem> items, String orderId) {
        this.customerId = customerId;
        this.items = items;
        this.orderId = orderId;
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

    public static class OrderItem {
        private final String productId;
        private final int quantity;
        private final BigDecimal price;

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
}