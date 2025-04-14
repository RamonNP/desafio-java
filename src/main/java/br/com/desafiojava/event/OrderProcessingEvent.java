package br.com.desafiojava.event;

import java.util.List;
import java.util.UUID;

public class OrderProcessingEvent {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;

    public OrderProcessingEvent() {
    }

    public OrderProcessingEvent(String orderId, String customerId, List<OrderItem> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

}
