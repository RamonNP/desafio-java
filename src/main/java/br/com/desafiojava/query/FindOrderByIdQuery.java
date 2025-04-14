package br.com.desafiojava.query;

public class FindOrderByIdQuery {
    private final String orderId;

    public FindOrderByIdQuery(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
