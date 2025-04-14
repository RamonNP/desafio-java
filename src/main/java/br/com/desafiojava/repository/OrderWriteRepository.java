package br.com.desafiojava.repository;

import br.com.desafiojava.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderWriteRepository extends JpaRepository<Order, UUID> {
}
