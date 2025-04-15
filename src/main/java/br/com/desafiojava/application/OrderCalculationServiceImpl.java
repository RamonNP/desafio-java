package br.com.desafiojava.application;

import br.com.desafiojava.event.OrderProcessingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class OrderCalculationServiceImpl implements OrderCalculationService {

    @Override
    public BigDecimal calculateTotalAmount(OrderProcessingEvent event) {
        if (event == null || event.getItems() == null) {
            log.error("Cannot calculate total amount: Event or items list is null");
            throw new IllegalArgumentException("Event or items cannot be null for total amount calculation");
        }

        return event.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}