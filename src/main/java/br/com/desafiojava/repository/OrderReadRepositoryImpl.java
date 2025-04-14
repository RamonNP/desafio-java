package br.com.desafiojava.repository;

import br.com.desafiojava.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderReadRepositoryImpl implements OrderReadRepository {

    private final OrderWriteRepository repository;

    public OrderReadRepositoryImpl(OrderWriteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repository.findById(id);
    }
}
