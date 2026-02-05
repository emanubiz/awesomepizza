package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.InvalidOrderStatusException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.OrderItemResponse;
import com.awesomepizza.order.application.service.api.IPizzaioloOrderService;
import com.awesomepizza.order.domain.Order;
import com.awesomepizza.order.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PizzaioloOrderServiceImpl implements IPizzaioloOrderService {

    private final OrderRepository orderRepository;

    public PizzaioloOrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(order -> {
                List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                    .map(item -> new OrderItemResponse(item.getId(), item.getPizzaName(), item.getQuantity(), item.getPrice()))
                    .toList();
                return new OrderResponse(
                    order.getId(),
                    order.getOrderCode(),
                    order.getStatus(),
                    order.getCustomerName(),
                    order.getPhone(),
                    order.getDeliveryAddress(),
                    order.getCreatedAt(),
                    itemResponses
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse takeOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        // Assuming "taking an order" means setting its status to IN_PREPARATION
        order.setStatus(OrderStatus.IN_PREPARATION);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = updatedOrder.getOrderItems().stream()
            .map(item -> new OrderItemResponse(item.getId(), item.getPizzaName(), item.getQuantity(), item.getPrice()))
            .toList();

        return new OrderResponse(
            updatedOrder.getId(),
            updatedOrder.getOrderCode(),
            updatedOrder.getStatus(),
            updatedOrder.getCustomerName(),
            updatedOrder.getPhone(),
            updatedOrder.getDeliveryAddress(),
            updatedOrder.getCreatedAt(),
            itemResponses
        );
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (newStatus == null) {
            throw new InvalidOrderStatusException("New order status cannot be null.");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = updatedOrder.getOrderItems().stream()
            .map(item -> new OrderItemResponse(item.getId(), item.getPizzaName(), item.getQuantity(), item.getPrice()))
            .toList();

        return new OrderResponse(
            updatedOrder.getId(),
            updatedOrder.getOrderCode(),
            updatedOrder.getStatus(),
            updatedOrder.getCustomerName(),
            updatedOrder.getPhone(),
            updatedOrder.getDeliveryAddress(),
            updatedOrder.getCreatedAt(),
            itemResponses
        );
    }
}
