package br.com.desafiojava.api;

import br.com.desafiojava.api.dto.CreateOrderRequestDto;
import br.com.desafiojava.api.mapper.OrderMapper;
import br.com.desafiojava.application.OrderApplicationService;
import br.com.desafiojava.common.exception.ApiError;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.OrderDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    public static final String INVALID_REQUEST = "Invalid Request";
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    public static final String ORDERS = "/orders";
    private final OrderApplicationService service;
    private final OrderMapper mapper;

    public OrderController(OrderApplicationService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequestDto request) {
        try {
            if (request == null) {
                logger.error("Received null order request");
                return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Order request cannot be null", ORDERS));
            }

            CreateOrderCommand command = mapper.toCreateOrderCommand(request);
            service.createOrder(command);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, e.getMessage(), ORDERS));

        } catch (IllegalStateException e) {
            logger.warn("Duplicate order request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(buildApiError(HttpStatus.CONFLICT, "Conflict", e.getMessage(), ORDERS));

        } catch (Exception e) {
            logger.error("Unexpected error while creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error occurred", ORDERS));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") String id) {
        try {
            if (id == null) {
                logger.error("Received null order ID");
                return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Order ID cannot be null", "/orders/" + id));
            }
            Optional<OrderDto> order = service.findOrderById(id);
            return order.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            logger.error("Unexpected error while retrieving order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error occurred", "/orders/" + id)
            );
        }
    }

    private ApiError buildApiError(HttpStatus status, String error, String message, String path) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
