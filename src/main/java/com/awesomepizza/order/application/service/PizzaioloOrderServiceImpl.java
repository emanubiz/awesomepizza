package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.InvalidOrderStatusException;
import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.mapper.OrderMapper;
import com.awesomepizza.order.application.service.api.IPizzaioloOrderService;
import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.domain.OrderStatusValidator;
import com.awesomepizza.order.domain.entity.Order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing pizza orders specifically from the
 * pizzaiolo's perspective.
 * Handles operations like fetching all orders, taking a specific order, taking
 * the next pending order,
 * and updating an order's status.
 */
@Slf4j
@Service
public class PizzaioloOrderServiceImpl implements IPizzaioloOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusValidator statusValidator;

    /**
     * Constructs a new PizzaioloOrderServiceImpl.
     *
     * @param orderRepository The repository for accessing order data.
     * @param orderMapper     The mapper for converting Order entities to
     *                        OrderResponse DTOs.
     * @param statusValidator The validator for checking order status transitions.
     */
    public PizzaioloOrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderStatusValidator statusValidator) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.statusValidator = statusValidator;
    }

    /**
     * Retrieves all orders, sorted by their creation date in ascending order.
     *
     * @return A list of {@link OrderResponse} representing all orders.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders for pizzaiolo, ordered by creation date");
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all orders with PENDING status, sorted by their creation date in
     * ascending order.
     *
     * @return A list of {@link OrderResponse} representing all pending orders.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllPendingOrders() {
        log.debug("Fetching all pending orders for pizzaiolo, ordered by creation date");
        return orderRepository.findByStatus(OrderStatus.PENDING).stream()
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Allows a pizzaiolo to take a specific order for preparation.
     * An order can only be taken if no other order is currently in 'IN_PREPARATION'
     * status
     * and the order itself is in 'PENDING' status.
     *
     * @param orderCode The unique code of the order to be taken.
     * @return An {@link OrderResponse} representing the updated order.
     * @throws OrderNotFoundException               if no order with the given code
     *                                              is found.
     * @throws OrderModificationNotAllowedException if another order is already in
     *                                              preparation
     *                                              or the target order is not in
     *                                              PENDING status.
     * @throws InvalidOrderStatusException          if the status transition is
     *                                              invalid.
     */
    @Override
    @Transactional
    public OrderResponse takeOrder(String orderCode) {
        log.info("Pizzaiolo attempting to take order: {}", orderCode);

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)) {
            log.warn("Cannot take order {}: another order is currently IN_PREPARATION.", orderCode);
            throw new OrderModificationNotAllowedException(
                    "Cannot take a new order because another order is currently IN_PREPARATION.");
        }

        if (!statusValidator.canBeTakenByPizzaiolo(order.getStatus())) {
            log.warn("Attempt to take order {} with status {}", orderCode, order.getStatus());
            throw new OrderModificationNotAllowedException(
                    "Order can only be taken if status is PENDING. Current status: " + order.getStatus());
        }

        statusValidator.validateTransition(order.getStatus(), OrderStatus.IN_PREPARATION);

        order.setStatus(OrderStatus.IN_PREPARATION);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} taken successfully, status changed to IN_PREPARATION", orderCode);

        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Allows a pizzaiolo to take the next available pending order.
     * The "next" order is defined as the oldest order (by creation date) that is in
     * 'PENDING' status.
     * An order can only be taken if no other order is currently in 'IN_PREPARATION'
     * status.
     *
     * @return An {@link OrderResponse} representing the updated order.
     * @throws OrderNotFoundException               if no pending orders are found
     *                                              to be taken.
     * @throws OrderModificationNotAllowedException if another order is already in
     *                                              preparation.
     * @throws InvalidOrderStatusException          if the status transition is
     *                                              invalid.
     */
    @Override
    @Transactional
    public OrderResponse takeNextOrder() {
        log.info("Pizzaiolo attempting to take the next pending order.");

        if (orderRepository.existsByStatus(OrderStatus.IN_PREPARATION)) {
            log.warn("Cannot take the next order: another order is currently IN_PREPARATION.");
            throw new OrderModificationNotAllowedException(
                    "Cannot take a new order because another order is currently IN_PREPARATION.");
        }

        Order nextPendingOrder = orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatus.PENDING)
                .orElseThrow(() -> {
                    log.info("No pending orders available to be taken.");
                    return new OrderNotFoundException("No pending orders found to be taken.");
                });

        statusValidator.validateTransition(nextPendingOrder.getStatus(), OrderStatus.IN_PREPARATION);

        nextPendingOrder.setStatus(OrderStatus.IN_PREPARATION);
        Order updatedOrder = orderRepository.save(nextPendingOrder);
        log.info("Next pending order {} taken successfully, status changed to IN_PREPARATION",
                updatedOrder.getOrderCode());

        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Updates the status of an existing order.
     *
     * @param orderCode The unique code of the order to be updated.
     * @param newStatus The new status to set for the order.
     * @return An {@link OrderResponse} representing the updated order.
     * @throws OrderNotFoundException      if no order with the given code is found.
     * @throws InvalidOrderStatusException if the new status is null or the status
     *                                     transition is invalid.
     */
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderCode, newStatus);

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (newStatus == null) {
            throw new InvalidOrderStatusException("New order status cannot be null.");
        }

        statusValidator.validateTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated successfully to {}", orderCode, newStatus);

        return orderMapper.toResponse(updatedOrder);
    }
}