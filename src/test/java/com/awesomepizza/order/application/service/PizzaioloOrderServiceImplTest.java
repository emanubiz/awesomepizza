package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.InvalidOrderStatusException;
import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.OrderResponse;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PizzaioloOrderService Unit Tests")
class PizzaioloOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderStatusValidator statusValidator;

    @InjectMocks
    private PizzaioloOrderServiceImpl pizzaioloOrderService;

    private Order testOrder;
    private Order pendingOrderOldest;
    private Order pendingOrderNewer;
    private Order inPreparationOrder;
    private OrderResponse testOrderResponse;
    private OrderResponse pendingOrderOldestResponse;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(UUID.randomUUID());
        testOrder.setOrderCode("ORD-TEST123");
        testOrder.setCustomerName("Mario Rossi");
        testOrder.setPhone("+393331234567");
        testOrder.setDeliveryAddress("Via Roma 1, Milano");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setCreatedAt(Instant.now().minus(2, ChronoUnit.HOURS));
        testOrder.setOrderItems(new ArrayList<>());

        testOrderResponse = new OrderResponse(
            testOrder.getId(),
            testOrder.getOrderCode(),
            testOrder.getStatus(),
            testOrder.getCustomerName(),
            testOrder.getPhone(),
            testOrder.getDeliveryAddress(),
            testOrder.getCreatedAt(),
            new ArrayList<>()
        );

        pendingOrderOldest = new Order();
        pendingOrderOldest.setId(UUID.randomUUID());
        pendingOrderOldest.setOrderCode("ORD-NEXT001");
        pendingOrderOldest.setCustomerName("Next Customer 1");
        pendingOrderOldest.setStatus(OrderStatus.PENDING);
        pendingOrderOldest.setCreatedAt(Instant.now().minus(3, ChronoUnit.HOURS));
        pendingOrderOldest.setOrderItems(new ArrayList<>());

        pendingOrderNewer = new Order();
        pendingOrderNewer.setId(UUID.randomUUID());
        pendingOrderNewer.setOrderCode("ORD-NEXT002");
        pendingOrderNewer.setCustomerName("Next Customer 2");
        pendingOrderNewer.setStatus(OrderStatus.PENDING);
        pendingOrderNewer.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        pendingOrderNewer.setOrderItems(new ArrayList<>());

        inPreparationOrder = new Order();
        inPreparationOrder.setId(UUID.randomUUID());
        inPreparationOrder.setOrderCode("ORD-INPREP");
        inPreparationOrder.setCustomerName("In Prep Customer");
        inPreparationOrder.setStatus(OrderStatus.IN_PREPARATION);
        inPreparationOrder.setCreatedAt(Instant.now().minus(4, ChronoUnit.HOURS));
        inPreparationOrder.setOrderItems(new ArrayList<>());

        pendingOrderOldestResponse = new OrderResponse(
            pendingOrderOldest.getId(),
            pendingOrderOldest.getOrderCode(),
            pendingOrderOldest.getStatus(),
            pendingOrderOldest.getCustomerName(),
            pendingOrderOldest.getPhone(),
            pendingOrderOldest.getDeliveryAddress(),
            pendingOrderOldest.getCreatedAt(),
            new ArrayList<>()
        );
    }

    @Test
    @DisplayName("Should get all orders sorted by creation date successfully")
    void shouldGetAllOrdersSuccessfullySortedByCreatedAt() {
        Order olderOrder = new Order();
        olderOrder.setId(UUID.randomUUID());
        olderOrder.setOrderCode("ORD-OLD");
        olderOrder.setStatus(OrderStatus.COMPLETED);
        olderOrder.setCreatedAt(Instant.now().minus(5, ChronoUnit.HOURS));
        olderOrder.setOrderItems(new ArrayList<>());

        Order newerOrder = new Order();
        newerOrder.setId(UUID.randomUUID());
        newerOrder.setOrderCode("ORD-NEW");
        newerOrder.setStatus(OrderStatus.PENDING);
        newerOrder.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        newerOrder.setOrderItems(new ArrayList<>());


        List<Order> ordersInDb = List.of(newerOrder, olderOrder);
        List<Order> expectedSortedOrders = List.of(olderOrder, newerOrder);

        when(orderRepository.findAll()).thenReturn(ordersInDb);
        when(orderMapper.toResponse(olderOrder)).thenReturn(new OrderResponse(
            olderOrder.getId(), olderOrder.getOrderCode(), olderOrder.getStatus(), null, null, null, olderOrder.getCreatedAt(), new ArrayList<>()));
        when(orderMapper.toResponse(newerOrder)).thenReturn(new OrderResponse(
            newerOrder.getId(), newerOrder.getOrderCode(), newerOrder.getStatus(), null, null, null, newerOrder.getCreatedAt(), new ArrayList<>()));


        List<OrderResponse> result = pizzaioloOrderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderCode()).isEqualTo("ORD-OLD");
        assertThat(result.get(1).orderCode()).isEqualTo("ORD-NEW");
        verify(orderRepository, times(1)).findAll();
        verify(orderMapper, times(1)).toResponse(olderOrder);
        verify(orderMapper, times(1)).toResponse(newerOrder);
    }

    @Test
    @DisplayName("Should take order successfully when status is PENDING and no other order is in preparation")
    void shouldTakeOrderSuccessfullyWhenStatusIsPendingAndNoOtherOrderInPreparation() {
        String orderCode = "ORD-TEST123";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(false);
        when(statusValidator.canBeTakenByPizzaiolo(OrderStatus.PENDING)).thenReturn(true);
        doNothing().when(statusValidator).validateTransition(OrderStatus.PENDING, OrderStatus.IN_PREPARATION);
        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        OrderResponse result = pizzaioloOrderService.takeOrder(orderCode);

        assertThat(result).isNotNull();
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(statusValidator, times(1)).validateTransition(OrderStatus.PENDING, OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when taking order if another order is already IN_PREPARATION")
    void shouldThrowExceptionWhenTakingOrderIfAnotherOrderIsInPreparation() {
        String orderCode = "ORD-TEST123";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(true);

        assertThatThrownBy(() -> pizzaioloOrderService.takeOrder(orderCode))
            .isInstanceOf(OrderModificationNotAllowedException.class)
            .hasMessageContaining("Cannot take a new order because another order is currently IN_PREPARATION.");

        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(statusValidator, never()).canBeTakenByPizzaiolo(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when taking non-PENDING order")
    void shouldThrowExceptionWhenTakingNonPendingOrder() {
        String orderCode = "ORD-TEST123";
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(false);
        when(statusValidator.canBeTakenByPizzaiolo(OrderStatus.COMPLETED)).thenReturn(false);

        assertThatThrownBy(() -> pizzaioloOrderService.takeOrder(orderCode))
            .isInstanceOf(OrderModificationNotAllowedException.class)
            .hasMessageContaining("can only be taken if status is PENDING");

        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(statusValidator, times(1)).canBeTakenByPizzaiolo(OrderStatus.COMPLETED);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update order status successfully with valid transition")
    void shouldUpdateOrderStatusSuccessfullyWithValidTransition() {
        String orderCode = "ORD-TEST123";
        testOrder.setStatus(OrderStatus.IN_PREPARATION);
        OrderStatus newStatus = OrderStatus.READY;

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        doNothing().when(statusValidator).validateTransition(OrderStatus.IN_PREPARATION, OrderStatus.READY);
        when(orderRepository.save(testOrder)).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(testOrderResponse);

        OrderResponse result = pizzaioloOrderService.updateOrderStatus(orderCode, newStatus);

        assertThat(result).isNotNull();
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.READY);
        verify(statusValidator, times(1)).validateTransition(OrderStatus.IN_PREPARATION, OrderStatus.READY);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should throw exception with invalid status transition")
    void shouldThrowExceptionWithInvalidStatusTransition() {
        String orderCode = "ORD-TEST123";
        testOrder.setStatus(OrderStatus.COMPLETED);
        OrderStatus newStatus = OrderStatus.PENDING;

        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));
        doThrow(new InvalidOrderStatusException("Invalid transition"))
            .when(statusValidator).validateTransition(OrderStatus.COMPLETED, OrderStatus.PENDING);

        assertThatThrownBy(() -> pizzaioloOrderService.updateOrderStatus(orderCode, newStatus))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("Invalid transition");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when order not found for operations")
    void shouldThrowExceptionWhenOrderNotFoundForOperations() {
        String orderCode = "ORD-NOTEXIST";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pizzaioloOrderService.takeOrder(orderCode))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("not found");

        assertThatThrownBy(() -> pizzaioloOrderService.updateOrderStatus(orderCode, OrderStatus.READY))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw exception when new status is null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        String orderCode = "ORD-TEST123";
        when(orderRepository.findByOrderCode(orderCode)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> pizzaioloOrderService.updateOrderStatus(orderCode, null))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should take the next pending order successfully when no other order is in preparation")
    void shouldTakeNextOrderSuccessfullyWhenNoOtherOrderInPreparation() {
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(false);
        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.PENDING))
            .thenReturn(Optional.of(pendingOrderOldest));
        doNothing().when(statusValidator).validateTransition(OrderStatus.PENDING, OrderStatus.IN_PREPARATION); // Corrected here
        when(orderRepository.save(pendingOrderOldest)).thenReturn(pendingOrderOldest);
        when(orderMapper.toResponse(pendingOrderOldest)).thenReturn(pendingOrderOldestResponse);

        OrderResponse result = pizzaioloOrderService.takeNextOrder();

        assertThat(result).isNotNull();
        assertThat(result.orderCode()).isEqualTo(pendingOrderOldest.getOrderCode());
        assertThat(pendingOrderOldest.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        verify(statusValidator, times(1)).validateTransition(OrderStatus.PENDING, OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).save(pendingOrderOldest);
        verify(orderMapper, times(1)).toResponse(pendingOrderOldest);
    }

    @Test
    @DisplayName("Should throw exception when taking next order if another order is already IN_PREPARATION")
    void shouldThrowExceptionWhenTakingNextOrderIfAnotherOrderIsInPreparation() {
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(true);

        assertThatThrownBy(() -> pizzaioloOrderService.takeNextOrder())
            .isInstanceOf(OrderModificationNotAllowedException.class)
            .hasMessageContaining("Cannot take a new order because another order is currently IN_PREPARATION.");

        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(orderRepository, never()).findFirstByStatusOrderByCreatedAtAsc(any());
        verify(statusValidator, never()).validateTransition(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when taking next order if no pending orders are available")
    void shouldThrowExceptionWhenNoPendingOrdersAvailableForTakeNextOrder() {
        when(orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)).thenReturn(false);
        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.PENDING))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> pizzaioloOrderService.takeNextOrder())
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("No pending orders found to be taken.");

        verify(orderRepository, times(1)).existsByStatus(OrderStatus.IN_PREPARATION);
        verify(orderRepository, times(1)).findFirstByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
        verify(statusValidator, never()).validateTransition(any(), any());
        verify(orderRepository, never()).save(any());
    }
}