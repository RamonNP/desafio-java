package br.com.desafiojava.application;


import br.com.desafiojava.event.OrderProcessingEvent;

import java.math.BigDecimal;

public interface OrderCalculationService {
    BigDecimal calculateTotalAmount(OrderProcessingEvent event);
}