package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;
import com.awesomepizza.order.application.mapper.OrderMapper;
import com.awesomepizza.order.application.service.api.ICustomerOrderService;
import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.domain.OrderStatusValidator;
import com.awesomepizza.order.domain.entity.Order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing pizza orders from a customer's perspective.
 * Handles operations such as creating new orders, retrieving order details,
 * updating existing orders, and canceling orders.
 */
@Slf4j
@Service
public class CustomerOrderServiceImpl implements ICustomerOrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusValidator statusValidator;

    /**
     * Constructs a new CustomerOrderServiceImpl.
     *
     * @param orderRepository   The repository for accessing order data.
     * @param orderMapper       The mapper for converting Order entities to OrderResponse DTOs and vice versa.
     * @param statusValidator   The validator for checking order status transitions and modification rules.
     */
    public CustomerOrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderStatusValidator statusValidator) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.statusValidator = statusValidator;
    }

    /**
     * Creates a new order based on the provided customer request.
     * The order is assigned a unique code and initialized with a 'PENDING' status.
     *
     * @param request The {@link CreateOrderRequest} containing details for the new order.
     * @return An {@link OrderResponse} representing the newly created order.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer: {}", request.customerName());

        Order order = orderMapper.toEntity(request);
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);
        log.info("Order {} created successfully", saved.getOrderCode());

        return orderMapper.toResponse(saved);
    }

    /**
     * Retrieves an order by its unique order code.
     *
     * @param orderCode The unique code of the order to retrieve.
     * @return An {@link Optional} containing the {@link OrderResponse} if found,
     *         or an empty Optional if no order matches the code.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getByOrderCode(String orderCode) {
        log.debug("Fetching order by code: {}", orderCode);
        return orderRepository.findByOrderCode(orderCode)
                .map(orderMapper::toResponse);
    }

    /**
     * Updates an existing order specified by its order code with new details.
     * An order can only be updated if its current status allows customer modifications.
     *
     * @param orderCode The unique code of the order to update.
     * @param request   The {@link UpdateOrderRequest} containing the updated order details.
     * @return An {@link OrderResponse} representing the updated order.
     * @throws OrderNotFoundException           if no order with the given code is found.
     * @throws OrderModificationNotAllowedException if the order's current status does not
     *                                              permit customer modifications.
     */
    @Override
    @Transactional
    public OrderResponse updateOrder(String orderCode, UpdateOrderRequest request) {
        log.info("Updating order: {}", orderCode);

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (!statusValidator.canBeModifiedByCustomer(order.getStatus())) {
            log.warn("Attempt to modify order {} with status {}", orderCode, order.getStatus());
            throw new OrderModificationNotAllowedException(
                    "Order cannot be updated as its status is " + order.getStatus());
        }

        orderMapper.updateEntity(order, request);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} updated successfully", orderCode);

        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Cancels an existing order specified by its order code.
     * An order can only be canceled if its current status allows customer modifications.
     * The order's status will be set to 'CANCELED'.
     *
     * @param orderCode The unique code of the order to cancel.
     * @return An {@link OrderResponse} representing the canceled order.
     * @throws OrderNotFoundException           if no order with the given code is found.
     * @throws OrderModificationNotAllowedException if the order's current status does not
     *                                              permit cancellation by the customer.
     */
    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderCode) {
        log.info("Canceling order: {}", orderCode);

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (!statusValidator.canBeModifiedByCustomer(order.getStatus())) {
            log.warn("Attempt to cancel order {} with status {}", orderCode, order.getStatus());
            throw new OrderModificationNotAllowedException(
                    "Order cannot be canceled as its status is " + order.getStatus());
        }

        statusValidator.validateTransition(order.getStatus(), OrderStatus.CANCELED);
        order.setStatus(OrderStatus.CANCELED);

        Order canceledOrder = orderRepository.save(order);
        log.info("Order {} canceled successfully", orderCode);

        return orderMapper.toResponse(canceledOrder);
    }
}