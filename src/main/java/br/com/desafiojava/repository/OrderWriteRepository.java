package br.com.desafiojava.repository;

import br.com.desafiojava.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderWriteRepository extends JpaRepository<Order, String> {
}
