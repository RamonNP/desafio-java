package br.com.desafiojava.query;

import br.com.desafiojava.domain.Order;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.mapper.OrderMapper;
import br.com.desafiojava.repository.OrderReadRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindOrderByIdQueryHandler implements QueryHandler<FindOrderByIdQuery, Optional<OrderDto>> {

    private final OrderReadRepository repository;
    private final OrderMapper mapper;

    public FindOrderByIdQueryHandler(OrderReadRepository repository, OrderMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<OrderDto> handle(FindOrderByIdQuery query) {
        return repository.findById(query.getOrderId())
                .map(mapper::toDto);
    }
}
