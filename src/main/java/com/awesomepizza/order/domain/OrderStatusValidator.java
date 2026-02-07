package com.awesomepizza.order.domain;

import org.springframework.stereotype.Component;

import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.exception.InvalidOrderStatusException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validator for order status transitions.
 * Implements a state machine to ensure only valid transitions are allowed.
 */
@Component
public class OrderStatusValidator {

    // Map of valid transitions: current status -> allowed next statuses
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING, EnumSet.of(
            OrderStatus.IN_PREPARATION,
            OrderStatus.CANCELED
        ),
        OrderStatus.IN_PREPARATION, EnumSet.of(
            OrderStatus.READY,
            OrderStatus.CANCELED
        ),
        OrderStatus.READY, EnumSet.of(
            OrderStatus.COMPLETED
        ),
        OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class),
        OrderStatus.CANCELED, EnumSet.noneOf(OrderStatus.class)
    );

    /**
     * Validates if a status transition is permitted.
     * 
     * @param currentStatus the current status of the order.
     * @param newStatus the desired new status.
     * @throws InvalidOrderStatusException if the transition is not valid.
     */
    public void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new InvalidOrderStatusException("Current status and new status cannot be null");
        }

        if (currentStatus == newStatus) {
            return;
        }

        Set<OrderStatus> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new InvalidOrderStatusException(
                String.format("Invalid status transition from %s to %s. Allowed transitions: %s",
                    currentStatus,
                    newStatus,
                    allowedTransitions != null ? allowedTransitions : "none"
                )
            );
        }
    }

    /**
     * Checks if an order can be modified by the customer.
     * 
     * @param currentStatus the current status of the order.
     * @return true if the order can be modified, false otherwise.
     */
    public boolean canBeModifiedByCustomer(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING;
    }

    /**
     * Checks if an order can be taken in charge by the pizzaiolo.
     * 
     * @param currentStatus the current status of the order.
     * @return true if the order can be taken in charge, false otherwise.
     */
    public boolean canBeTakenByPizzaiolo(OrderStatus currentStatus) {
        return currentStatus == OrderStatus.PENDING;
    }
}