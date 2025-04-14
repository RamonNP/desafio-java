package br.com.desafiojava.repository;

import br.com.desafiojava.domain.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderReadRepository {
    Optional<Order> findById(UUID id);
}
