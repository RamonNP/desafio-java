package br.com.desafiojava.repository;

import br.com.desafiojava.domain.Order;

import java.util.Optional;

public interface OrderReadRepository {
    Optional<Order> findById(String id);
}
