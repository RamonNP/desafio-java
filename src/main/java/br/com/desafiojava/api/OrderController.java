package br.com.desafiojava.api;

import br.com.desafiojava.api.dto.CreateOrderRequestDto;
import br.com.desafiojava.api.mapper.OrderMapper;
import br.com.desafiojava.application.OrderApplicationService;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderApplicationService service;
    private final OrderMapper mapper;

    public OrderController(OrderApplicationService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequestDto request) {
        CreateOrderCommand command = mapper.toCreateOrderCommand(request);
        service.createOrder(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable("id") UUID id) {
        Optional<OrderDto> order = service.findOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}