package br.com.desafiojava.domain;

import java.util.List;

public class CreateOrderRequestDto {
    private String customerId;
    private List<OrderItemDto> items;
    private String orderId;


    public CreateOrderRequestDto() {}

    public CreateOrderRequestDto(String customerId, List<OrderItemDto> items, String orderId) {
        this.customerId = customerId;
        this.items = items;
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}