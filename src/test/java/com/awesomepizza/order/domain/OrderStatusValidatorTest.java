package com.awesomepizza.order.domain;
import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.common.exception.InvalidOrderStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderStatusValidator Unit Tests")
class OrderStatusValidatorTest {

    private OrderStatusValidator validator;

    @BeforeEach
    void setUp() {
        validator = new OrderStatusValidator();
    }

    @Test
    @DisplayName("Should allow valid transition from PENDING to IN_PREPARATION")
    void shouldAllowValidTransitionFromPendingToInPreparation() {
        // When & Then
        assertThatCode(() -> validator.validateTransition(OrderStatus.PENDING, OrderStatus.IN_PREPARATION))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow valid transition from PENDING to CANCELED")
    void shouldAllowValidTransitionFromPendingToCanceled() {
        // When & Then
        assertThatCode(() -> validator.validateTransition(OrderStatus.PENDING, OrderStatus.CANCELED))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow valid transition from IN_PREPARATION to READY")
    void shouldAllowValidTransitionFromInPreparationToReady() {
        // When & Then
        assertThatCode(() -> validator.validateTransition(OrderStatus.IN_PREPARATION, OrderStatus.READY))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should allow valid transition from READY to COMPLETED")
    void shouldAllowValidTransitionFromReadyToCompleted() {
        // When & Then
        assertThatCode(() -> validator.validateTransition(OrderStatus.READY, OrderStatus.COMPLETED))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject invalid transition from PENDING to READY")
    void shouldRejectInvalidTransitionFromPendingToReady() {
        // When & Then
        assertThatThrownBy(() -> validator.validateTransition(OrderStatus.PENDING, OrderStatus.READY))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should reject invalid transition from COMPLETED to PENDING")
    void shouldRejectInvalidTransitionFromCompletedToPending() {
        // When & Then
        assertThatThrownBy(() -> validator.validateTransition(OrderStatus.COMPLETED, OrderStatus.PENDING))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should reject transition from CANCELED to any status")
    void shouldRejectTransitionFromCanceledToAnyStatus() {
        // When & Then
        assertThatThrownBy(() -> validator.validateTransition(OrderStatus.CANCELED, OrderStatus.PENDING))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Should allow same status transition")
    void shouldAllowSameStatusTransition() {
        // When & Then
        assertThatCode(() -> validator.validateTransition(OrderStatus.PENDING, OrderStatus.PENDING))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw exception when current status is null")
    void shouldThrowExceptionWhenCurrentStatusIsNull() {
        // When & Then
        assertThatThrownBy(() -> validator.validateTransition(null, OrderStatus.PENDING))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when new status is null")
    void shouldThrowExceptionWhenNewStatusIsNull() {
        // When & Then
        assertThatThrownBy(() -> validator.validateTransition(OrderStatus.PENDING, null))
            .isInstanceOf(InvalidOrderStatusException.class)
            .hasMessageContaining("cannot be null");
    }

    @ParameterizedTest
    @MethodSource("provideStatusesForCustomerModification")
    @DisplayName("Should correctly identify if order can be modified by customer")
    void shouldCorrectlyIdentifyIfOrderCanBeModifiedByCustomer(OrderStatus status, boolean expected) {
        // When
        boolean result = validator.canBeModifiedByCustomer(status);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideStatusesForPizzaioloTake")
    @DisplayName("Should correctly identify if order can be taken by pizzaiolo")
    void shouldCorrectlyIdentifyIfOrderCanBeTakenByPizzaiolo(OrderStatus status, boolean expected) {
        // When
        boolean result = validator.canBeTakenByPizzaiolo(status);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideStatusesForCustomerModification() {
        return Stream.of(
            Arguments.of(OrderStatus.PENDING, true),
            Arguments.of(OrderStatus.IN_PREPARATION, false),
            Arguments.of(OrderStatus.READY, false),
            Arguments.of(OrderStatus.COMPLETED, false),
            Arguments.of(OrderStatus.CANCELED, false)
        );
    }

    private static Stream<Arguments> provideStatusesForPizzaioloTake() {
        return Stream.of(
            Arguments.of(OrderStatus.PENDING, true),
            Arguments.of(OrderStatus.IN_PREPARATION, false),
            Arguments.of(OrderStatus.READY, false),
            Arguments.of(OrderStatus.COMPLETED, false),
            Arguments.of(OrderStatus.CANCELED, false)
        );
    }
}