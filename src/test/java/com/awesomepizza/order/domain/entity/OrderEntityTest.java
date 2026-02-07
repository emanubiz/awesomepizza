package com.awesomepizza.order.domain.entity;

import com.awesomepizza.order.domain.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Entity Unit Tests")
class OrderEntityTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setCustomerName("Test Customer");
        order.setPhone("+393331234567");
        order.setDeliveryAddress("Test Address");
        order.setStatus(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should add order item and set bidirectional relationship")
    void shouldAddOrderItemAndSetBidirectionalRelationship() {
        // Given
        OrderItem item = new OrderItem();
        item.setPizzaName("Margherita");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("8.50"));

        // When
        order.addOrderItem(item);

        // Then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems().get(0)).isEqualTo(item);
        assertThat(item.getOrder()).isEqualTo(order); // Verifica relazione bidirezionale
    }

    @Test
    @DisplayName("Should add multiple order items")
    void shouldAddMultipleOrderItems() {
        // Given
        OrderItem item1 = new OrderItem();
        item1.setPizzaName("Margherita");
        item1.setQuantity(1);
        item1.setPrice(new BigDecimal("8.00"));

        OrderItem item2 = new OrderItem();
        item2.setPizzaName("Diavola");
        item2.setQuantity(2);
        item2.setPrice(new BigDecimal("9.00"));

        OrderItem item3 = new OrderItem();
        item3.setPizzaName("Quattro Formaggi");
        item3.setQuantity(1);
        item3.setPrice(new BigDecimal("10.00"));

        // When
        order.addOrderItem(item1);
        order.addOrderItem(item2);
        order.addOrderItem(item3);

        // Then
        assertThat(order.getOrderItems()).hasSize(3);
        assertThat(order.getOrderItems()).containsExactly(item1, item2, item3);
        assertThat(item1.getOrder()).isEqualTo(order);
        assertThat(item2.getOrder()).isEqualTo(order);
        assertThat(item3.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should remove order item and clear bidirectional relationship")
    void shouldRemoveOrderItemAndClearBidirectionalRelationship() {
        // Given
        OrderItem item1 = new OrderItem();
        item1.setPizzaName("Margherita");
        item1.setQuantity(1);
        item1.setPrice(new BigDecimal("8.00"));

        OrderItem item2 = new OrderItem();
        item2.setPizzaName("Diavola");
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("9.00"));

        order.addOrderItem(item1);
        order.addOrderItem(item2);

        // When
        order.removeOrderItem(item1);

        // Then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems()).containsOnly(item2);
        assertThat(item1.getOrder()).isNull(); // Relazione bidirezionale rimossa
        assertThat(item2.getOrder()).isEqualTo(order); // Relazione mantenuta
    }

    @Test
    @DisplayName("Should initialize with empty order items list")
    void shouldInitializeWithEmptyOrderItemsList() {
        // Given
        Order newOrder = new Order();

        // Then
        assertThat(newOrder.getOrderItems()).isNotNull();
        assertThat(newOrder.getOrderItems()).isEmpty();
    }

    @Test
    @DisplayName("Should initialize with PENDING status by default")
    void shouldInitializeWithPendingStatusByDefault() {
        // Given
        Order newOrder = new Order();

        // Then
        assertThat(newOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should set and get all order properties correctly")
    void shouldSetAndGetAllOrderPropertiesCorrectly() {
        // Given & When
        order.setOrderCode("ORD-TEST123");
        order.setCustomerName("Mario Rossi");
        order.setPhone("+393331234567");
        order.setDeliveryAddress("Via Roma 1, Milano");
        order.setStatus(OrderStatus.IN_PREPARATION);

        // Then
        assertThat(order.getOrderCode()).isEqualTo("ORD-TEST123");
        assertThat(order.getCustomerName()).isEqualTo("Mario Rossi");
        assertThat(order.getPhone()).isEqualTo("+393331234567");
        assertThat(order.getDeliveryAddress()).isEqualTo("Via Roma 1, Milano");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }
}