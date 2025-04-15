package br.com.desafiojava.application;

import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.domain.OrderStatusDto;

import java.util.Optional;

public interface OrderApplicationService {
    void createOrder(CreateOrderCommand command);
    Optional<OrderDto> findOrderById(String orderId);
    Optional<OrderStatusDto> findOrderStatusById(String orderId);
}