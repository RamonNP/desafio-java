package br.com.desafiojava.application;

import br.com.desafiojava.command.CreateOrderCommandHandler;
import br.com.desafiojava.common.exception.KafkaProcessingException;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.query.FindOrderByIdQuery;
import br.com.desafiojava.query.FindOrderByIdQueryHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final CreateOrderCommandHandler createHandler;
    private final FindOrderByIdQueryHandler findHandler;

    public OrderApplicationServiceImpl(CreateOrderCommandHandler createHandler, FindOrderByIdQueryHandler findHandler) {
        this.createHandler = createHandler;
        this.findHandler = findHandler;
    }

    @Override
    public void createOrder(CreateOrderCommand command) {
            createHandler.handle(command);
    }

    @Override
    public Optional<OrderDto> findOrderById(String orderId) {
        try {
            if (orderId == null) {
                log.error("Received null orderId");
                throw new IllegalArgumentException("Order ID cannot be null");
            }
            return findHandler.handle(new FindOrderByIdQuery(orderId));
        } catch (IllegalArgumentException e) {
            log.error("Invalid order ID: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve order: " + e.getMessage());
        }
    }
}