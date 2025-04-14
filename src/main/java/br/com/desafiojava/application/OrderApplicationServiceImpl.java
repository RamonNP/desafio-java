package br.com.desafiojava.application;

import br.com.desafiojava.command.CreateOrderCommandHandler;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.query.FindOrderByIdQuery;
import br.com.desafiojava.query.FindOrderByIdQueryHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
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
    public Optional<OrderDto> findOrderById(UUID orderId) {
        return findHandler.handle(new FindOrderByIdQuery(orderId));
    }
}