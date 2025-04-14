package br.com.desafiojava.api.mapper;

import br.com.desafiojava.api.dto.CreateOrderRequestDto;
import br.com.desafiojava.api.dto.OrderItemDto;
import br.com.desafiojava.domain.CreateOrderCommand;
import br.com.desafiojava.event.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "orderId", source = "orderId")
    CreateOrderCommand toCreateOrderCommand(CreateOrderRequestDto dto);

    CreateOrderCommand.OrderItem toOrderItem(OrderItemDto dto);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    OrderItem toEventOrderItem(CreateOrderCommand.OrderItem orderItem);
}