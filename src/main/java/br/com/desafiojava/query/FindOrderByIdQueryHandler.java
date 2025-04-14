package br.com.desafiojava.query;

import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.repository.OrderReadRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindOrderByIdQueryHandler implements QueryHandler<FindOrderByIdQuery, Optional<OrderDto>> {

    private final OrderReadRepository repository;

    public FindOrderByIdQueryHandler(OrderReadRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrderDto> handle(FindOrderByIdQuery query) {
        Optional<Order> order = repository.findById(query.getOrderId());
        return order.map(o -> new OrderDto(
            o.getId(),
            o.getCustomerId(),
            o.getTotalAmount(),
            o.getCreatedAt(),
            o.getStatus()
        ));
    }
}
