package br.com.desafiojava.api;

import br.com.desafiojava.application.OrderApplicationService;
import br.com.desafiojava.domain.CreateOrderRequestDto;
import br.com.desafiojava.domain.OrderDto;
import br.com.desafiojava.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderApplicationService service;

    @MockBean
    private OrderMapper mapper;

    @Test
    public void shouldCreateOrderSuccessfully() throws Exception {
        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setOrderId("123");
        request.setCustomerId("cust-1");

        when(mapper.toCreateOrderCommand(any(CreateOrderRequestDto.class))).thenReturn(null); // Simula mapeamento

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"123\",\"customerId\":\"cust-1\"}"))
                .andExpect(status().isOk());

        verify(service, times(1)).createOrder(any());
    }

    @Test
    public void shouldReturnOrderWhenIdExists() throws Exception {
        OrderDto orderDto = new OrderDto("123", "cust-1", null, null, null, null);
        when(service.findOrderById("123")).thenReturn(Optional.of(orderDto));

        mockMvc.perform(get("/orders/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    public void shouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        when(service.findOrderById("999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldHandleExceptionWhenCreatingOrder() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(service).createOrder(any());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"123\",\"customerId\":\"cust-1\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }
}