package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.OrderItemRequest;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;
import com.awesomepizza.order.application.mapper.OrderMapper;
import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.domain.OrderStatusValidator;
import com.awesomepizza.order.domain.entity.Order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerOrderService Unit Tests")
class CustomerOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStatusValidator statusValidator;

    @InjectMocks
    private CustomerOrderServiceImpl customerOrderService;

    private Order testOrder;
    private OrderResponse testOrderResponse;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setOrderCode("ORD-TEST123");
        testOrder.setCustomerName("Mario Rossi");
        testOrder.setPhone("+393331234567");
        testOrder.setDeliveryAddress("Via Roma 1, Milano");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(Instant.now());

        testOrderResponse = new OrderResponse(
                testOrder.getId(),
                testOrder.getOrderCode(),
                testOrder.getStatus(),
                testOrder.getCustomerName(),
                testOrder.getPhone(),
                testOrder.getDeliveryAddress(),
                testOrder.getCreatedAt(),
                new ArrayList<>());
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest(
                "Margherita",
                2,
                new BigDecimal("8.50"));
        CreateOrderRequest request = new CreateOrderRequest(
                "Mario Rossi",
                "+393331234567",
                "Via Roma 1, Milano",
                List.of(itemRequest));

        // Stub per far restituire un oggetto Order reale dal mock
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        // When
        OrderResponse result = customerOrderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isEqualTo("ORD-TEST123");
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderMapper, times(1)).toResponse(any(Order.class));
    }

    @Test
    @DisplayName("Should get order by code successfully")
    void shouldGetOrderByCodeSuccessfully() {
        // Given
        String orderCode = "ORD-TEST123";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        // When
        Optional<OrderResponse> result = customerOrderService.getByOrderCode(orderCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().orderCode()).isEqualTo(orderCode);
        verify(orderRepository, times(1)).findByOrderCode(orderCode);
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        String orderCode = "ORD-NOTEXIST";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.empty());

        // When
        Optional<OrderResponse> result = customerOrderService.getByOrderCode(orderCode);

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findByOrderCode(orderCode);
        verify(orderMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should update order when status is PENDING")
    void shouldUpdateOrderWhenStatusIsPending() {
        String orderCode = "ORD-TEST123";
        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
                Optional.of("Luigi Verdi"),
                Optional.of("+393337654321"),
                Optional.of("Via Nuova 10, Milano"),
                Optional.empty());

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(statusValidator.canBeModifiedByCustomer(OrderStatus.PENDING)).thenReturn(true);
        doAnswer(invocation -> {
            Order orderArg = invocation.getArgument(0);
            UpdateOrderRequest req = invocation.getArgument(1);
            req.customerName().ifPresent(orderArg::setCustomerName);
            req.phone().ifPresent(orderArg::setPhone);
            req.deliveryAddress().ifPresent(orderArg::setDeliveryAddress);
            return null;
        }).when(orderMapper).updateEntity(any(Order.class), any(UpdateOrderRequest.class));

        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        OrderResponse result = customerOrderService.updateOrder(orderCode, updateRequest);

        assertThat(result).isNotNull();
        assertThat(testOrder.getCustomerName()).isEqualTo("Luigi Verdi");
        assertThat(testOrder.getPhone()).isEqualTo("+393337654321");
        assertThat(testOrder.getDeliveryAddress()).isEqualTo("Via Nuova 10, Milano");

        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when updating non-PENDING order")
    void shouldThrowExceptionWhenUpdatingNonPendingOrder() {
        // Given
        String orderCode = "ORD-TEST123";
        testOrder.setStatus(OrderStatus.IN_PREPARATION);
        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
                Optional.of("Luigi Verdi"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(statusValidator.canBeModifiedByCustomer(OrderStatus.IN_PREPARATION)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> customerOrderService.updateOrder(orderCode, updateRequest))
                .isInstanceOf(OrderModificationNotAllowedException.class)
                .hasMessageContaining("cannot be updated");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should cancel order when status is PENDING")
    void shouldCancelOrderWhenStatusIsPending() {
        // Given
        String orderCode = "ORD-TEST123";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(statusValidator.canBeModifiedByCustomer(OrderStatus.PENDING)).thenReturn(true);
        doNothing().when(statusValidator).validateTransition(OrderStatus.PENDING, OrderStatus.CANCELED);
        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        // When
        OrderResponse result = customerOrderService.cancelOrder(orderCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(statusValidator, times(1)).validateTransition(OrderStatus.PENDING, OrderStatus.CANCELED);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when order not found for update")
    void shouldThrowExceptionWhenOrderNotFoundForUpdate() {
        // Given
        String orderCode = "ORD-NOTEXIST";
        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
                Optional.of("Luigi Verdi"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerOrderService.updateOrder(orderCode, updateRequest))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw exception when order not found for cancel")
    void shouldThrowExceptionWhenOrderNotFoundForCancel() {
        // Given
        String orderCode = "ORD-NOTEXIST";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerOrderService.cancelOrder(orderCode))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("not found");
    }
}