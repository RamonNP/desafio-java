package br.com.desafiojava.mapper;

import br.com.desafiojava.domain.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    public void shouldMapCreateOrderRequestDtoToCreateOrderCommand() {
        CreateOrderRequestDto dto = new CreateOrderRequestDto();
        dto.setOrderId("123");
        dto.setCustomerId("cust-1");
        dto.setItems(List.of(new OrderItemDto("prod-1", 1, BigDecimal.TEN)));

        CreateOrderCommand command = mapper.toCreateOrderCommand(dto);
        assertEquals("123", command.getOrderId());
        assertEquals("cust-1", command.getCustomerId());
        assertEquals(1, command.getItems().size());
        assertEquals("prod-1", command.getItems().get(0).getProductId());
    }

    @Test
    public void shouldMapOrderToOrderDto() {
        Order order = Order.builder()
                .id("123")
                .customerId("cust-1")
                .totalAmount(BigDecimal.valueOf(20))
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.PROCESSED)
                .items(List.of(OrderItemEntity.builder()
                        .productId("prod-1")
                        .quantity(2)
                        .price(BigDecimal.TEN)
                        .build()))
                .build();

        OrderDto dto = mapper.toDto(order);
        assertEquals("123", dto.getId());
        assertEquals("cust-1", dto.getCustomerId());
        assertEquals(BigDecimal.valueOf(20), dto.getTotalAmount());
        assertEquals(OrderStatus.PROCESSED, dto.getStatus());
        assertEquals(1, dto.getItems().size());
        assertEquals("prod-1", dto.getItems().get(0).getProductId());
    }
}