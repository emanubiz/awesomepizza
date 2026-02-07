package com.awesomepizza.order.mapper;

import com.awesomepizza.order.domain.entity.Order;
import com.awesomepizza.order.domain.entity.OrderItem;
import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.dto.CreateOrderRequest;
import com.awesomepizza.order.dto.OrderItemRequest;
import com.awesomepizza.order.dto.OrderResponse;
import com.awesomepizza.order.dto.UpdateOrderRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderMapper Unit Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    @DisplayName("Should map Order entity to OrderResponse DTO")
    void shouldMapOrderToOrderResponse() {
        // Given
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderCode("ORD-TEST123");
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerName("Mario Rossi");
        order.setPhone("+393331234567");
        order.setDeliveryAddress("Via Roma 1, Milano");
        order.setCreatedAt(Instant.now());

        OrderItem item1 = new OrderItem();
        item1.setId(UUID.randomUUID());
        item1.setPizzaName("Margherita");
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("8.50"));
        order.addOrderItem(item1);

        OrderItem item2 = new OrderItem();
        item2.setId(UUID.randomUUID());
        item2.setPizzaName("Diavola");
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("9.00"));
        order.addOrderItem(item2);

        // When
        OrderResponse response = orderMapper.toResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(order.getId());
        assertThat(response.orderCode()).isEqualTo("ORD-TEST123");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.customerName()).isEqualTo("Mario Rossi");
        assertThat(response.phone()).isEqualTo("+393331234567");
        assertThat(response.deliveryAddress()).isEqualTo("Via Roma 1, Milano");
        assertThat(response.createdAt()).isEqualTo(order.getCreatedAt());
        
        assertThat(response.orderItems()).hasSize(2);
        assertThat(response.orderItems().get(0).pizzaName()).isEqualTo("Margherita");
        assertThat(response.orderItems().get(0).quantity()).isEqualTo(2);
        assertThat(response.orderItems().get(0).price()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(response.orderItems().get(1).pizzaName()).isEqualTo("Diavola");
        assertThat(response.orderItems().get(1).quantity()).isEqualTo(1);
        assertThat(response.orderItems().get(1).price()).isEqualByComparingTo(new BigDecimal("9.00"));
    }

    @Test
    @DisplayName("Should map Order entity with empty items list to OrderResponse")
    void shouldMapOrderWithEmptyItemsToOrderResponse() {
        // Given
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderCode("ORD-EMPTY");
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerName("Test User");
        order.setPhone("+393331111111");
        order.setDeliveryAddress("Test Address");
        order.setCreatedAt(Instant.now());

        // When
        OrderResponse response = orderMapper.toResponse(order);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.orderItems()).isEmpty();
    }

    @Test
    @DisplayName("Should map CreateOrderRequest DTO to Order entity")
    void shouldMapCreateOrderRequestToOrder() {
        // Given
        OrderItemRequest itemRequest1 = new OrderItemRequest(
            "Margherita",
            2,
            new BigDecimal("8.50")
        );
        
        OrderItemRequest itemRequest2 = new OrderItemRequest(
            "Quattro Formaggi",
            1,
            new BigDecimal("10.00")
        );

        CreateOrderRequest request = new CreateOrderRequest(
            "Luigi Verdi",
            "+393339876543",
            "Corso Venezia 15, Milano",
            List.of(itemRequest1, itemRequest2)
        );

        // When
        Order order = orderMapper.toEntity(request);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getCustomerName()).isEqualTo("Luigi Verdi");
        assertThat(order.getPhone()).isEqualTo("+393339876543");
        assertThat(order.getDeliveryAddress()).isEqualTo("Corso Venezia 15, Milano");
        
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderItems().get(0).getPizzaName()).isEqualTo("Margherita");
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(order.getOrderItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(order.getOrderItems().get(0).getOrder()).isEqualTo(order); // Verifica bidirectional relationship
        
        assertThat(order.getOrderItems().get(1).getPizzaName()).isEqualTo("Quattro Formaggi");
        assertThat(order.getOrderItems().get(1).getQuantity()).isEqualTo(1);
        assertThat(order.getOrderItems().get(1).getPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(order.getOrderItems().get(1).getOrder()).isEqualTo(order); // Verifica bidirectional relationship
    }

    @Test
    @DisplayName("Should update Order entity with UpdateOrderRequest - full update")
    void shouldUpdateOrderWithFullUpdateRequest() {
        // Given
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setOrderCode("ORD-UPDATE");
        order.setCustomerName("Original Name");
        order.setPhone("+393331111111");
        order.setDeliveryAddress("Original Address");
        order.setStatus(OrderStatus.PENDING);

        OrderItem originalItem = new OrderItem();
        originalItem.setPizzaName("Original Pizza");
        originalItem.setQuantity(1);
        originalItem.setPrice(new BigDecimal("5.00"));
        order.addOrderItem(originalItem);

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            Optional.of("Updated Name"),
            Optional.of("+393332222222"),
            Optional.of("Updated Address"),
            Optional.of(List.of(
                new OrderItemRequest("New Pizza", 3, new BigDecimal("12.00"))
            ))
        );

        // When
        orderMapper.updateEntity(order, updateRequest);

        // Then
        assertThat(order.getCustomerName()).isEqualTo("Updated Name");
        assertThat(order.getPhone()).isEqualTo("+393332222222");
        assertThat(order.getDeliveryAddress()).isEqualTo("Updated Address");
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getPizzaName()).isEqualTo("New Pizza");
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(order.getOrderItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("12.00"));
    }

    @Test
    @DisplayName("Should update Order entity with UpdateOrderRequest - partial update")
    void shouldUpdateOrderWithPartialUpdateRequest() {
        // Given
        Order order = new Order();
        order.setCustomerName("Original Name");
        order.setPhone("+393331111111");
        order.setDeliveryAddress("Original Address");

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            Optional.of("Updated Name"),
            Optional.empty(),  // Phone non cambia
            Optional.empty(),  // Address non cambia
            Optional.empty()   // Items non cambiano
        );

        // When
        orderMapper.updateEntity(order, updateRequest);

        // Then
        assertThat(order.getCustomerName()).isEqualTo("Updated Name");
        assertThat(order.getPhone()).isEqualTo("+393331111111"); // Non modificato
        assertThat(order.getDeliveryAddress()).isEqualTo("Original Address"); // Non modificato
    }

    @Test
    @DisplayName("Should update Order entity with UpdateOrderRequest - empty optionals")
    void shouldNotUpdateOrderWhenAllOptionalsAreEmpty() {
        // Given
        Order order = new Order();
        order.setCustomerName("Original Name");
        order.setPhone("+393331111111");
        order.setDeliveryAddress("Original Address");

        OrderItem item = new OrderItem();
        item.setPizzaName("Pizza");
        item.setQuantity(1);
        item.setPrice(new BigDecimal("10.00"));
        order.addOrderItem(item);

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );

        // When
        orderMapper.updateEntity(order, updateRequest);

        // Then - Nothing should change
        assertThat(order.getCustomerName()).isEqualTo("Original Name");
        assertThat(order.getPhone()).isEqualTo("+393331111111");
        assertThat(order.getDeliveryAddress()).isEqualTo("Original Address");
        assertThat(order.getOrderItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should clear and replace order items when updating with new items list")
    void shouldClearAndReplaceOrderItemsWhenUpdating() {
        // Given
        Order order = new Order();
        
        OrderItem oldItem1 = new OrderItem();
        oldItem1.setPizzaName("Old Pizza 1");
        oldItem1.setQuantity(1);
        oldItem1.setPrice(new BigDecimal("5.00"));
        order.addOrderItem(oldItem1);
        
        OrderItem oldItem2 = new OrderItem();
        oldItem2.setPizzaName("Old Pizza 2");
        oldItem2.setQuantity(2);
        oldItem2.setPrice(new BigDecimal("6.00"));
        order.addOrderItem(oldItem2);

        UpdateOrderRequest updateRequest = new UpdateOrderRequest(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(List.of(
                new OrderItemRequest("New Pizza", 1, new BigDecimal("15.00"))
            ))
        );

        // When
        orderMapper.updateEntity(order, updateRequest);

        // Then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0).getPizzaName()).isEqualTo("New Pizza");
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(1);
        assertThat(order.getOrderItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("15.00"));
    }
}