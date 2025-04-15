package br.com.desafiojava.api;

import br.com.desafiojava.application.OrderApplicationService;
import br.com.desafiojava.common.exception.ApiError;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.domain.CreateOrderRequestDto;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.domain.OrderStatusDto;
import br.com.desafiojava.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    public static final String INVALID_REQUEST = "Invalid Request";
    public static final String UNEXPECTED_ERROR_OCCURRED = "Unexpected error occurred";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
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
                log.error("Received null order request");
                return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Order request cannot be null", ORDERS));
            }

            CreateOrderCommand command = mapper.toCreateOrderCommand(request);
            service.createOrder(command);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.error("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, e.getMessage(), ORDERS));

        } catch (IllegalStateException e) {
            log.warn("Duplicate order request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(buildApiError(HttpStatus.CONFLICT, "Conflict", e.getMessage(), ORDERS));

        } catch (Exception e) {
            log.error("Unexpected error while creating order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_OCCURRED, ORDERS));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable("id") String id) {
        try {
            if (id == null) {
                log.error("Received null order ID");
                return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Order ID cannot be null", ORDERS + id));
            }
            Optional<OrderDto> order = service.findOrderById(id);
            return order.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Unexpected error while retrieving order {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_OCCURRED, ORDERS + id)
            );
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable("id") String id) {
        try {
            if (id == null) {
                log.error("Received null order ID for status");
                return ResponseEntity.badRequest().body(buildApiError(HttpStatus.BAD_REQUEST, INVALID_REQUEST, "Order ID cannot be null", ORDERS + id + "/status"));
            }
            Optional<OrderStatusDto> status = service.findOrderStatusById(id);
            return status.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving order status for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR_OCCURRED, ORDERS + id + "/status")
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