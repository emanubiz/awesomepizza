package com.awesomepizza.order.application.service;

import com.awesomepizza.common.exception.OrderModificationNotAllowedException;
import com.awesomepizza.common.exception.OrderNotFoundException;
import com.awesomepizza.order.adapter.out.persistence.OrderRepository;
import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;
import com.awesomepizza.order.application.service.api.ICustomerOrderService;
import com.awesomepizza.order.domain.Order;
import com.awesomepizza.order.domain.OrderItem;
import com.awesomepizza.order.domain.OrderStatus;
import com.awesomepizza.order.application.dto.OrderItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerOrderServiceImpl implements ICustomerOrderService {

    private final OrderRepository orderRepository;

    public CustomerOrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomerName(request.customerName());
        order.setPhone(request.phone());
        order.setDeliveryAddress(request.deliveryAddress());
        order.setStatus(OrderStatus.PENDING);

        request.orderItems().forEach(itemRequest -> {
            OrderItem item = new OrderItem();
            item.setPizzaName(itemRequest.pizzaName());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(itemRequest.price());
            order.addOrderItem(item);
        });

        Order saved = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = saved.getOrderItems().stream()
            .map(item -> new OrderItemResponse(item.getId(), item.getPizzaName(), item.getQuantity(), item.getPrice()))
            .toList();

        return new OrderResponse(
            saved.getId(),
            saved.getOrderCode(),
            saved.getStatus(),
            saved.getCustomerName(),
            saved.getPhone(),
            saved.getDeliveryAddress(),
            saved.getCreatedAt(),
            itemResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
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
            });
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String orderCode, UpdateOrderRequest request) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderModificationNotAllowedException("Order cannot be updated as its status is " + order.getStatus());
        }

        request.customerName().ifPresent(order::setCustomerName);
        request.phone().ifPresent(order::setPhone);
        request.deliveryAddress().ifPresent(order::setDeliveryAddress);

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
    public OrderResponse cancelOrder(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new OrderNotFoundException("Order with code " + orderCode + " not found."));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderModificationNotAllowedException("Order cannot be canceled as its status is " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);
        Order canceledOrder = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = canceledOrder.getOrderItems().stream()
            .map(item -> new OrderItemResponse(item.getId(), item.getPizzaName(), item.getQuantity(), item.getPrice()))
            .toList();

        return new OrderResponse(
            canceledOrder.getId(),
            canceledOrder.getOrderCode(),
            canceledOrder.getStatus(),
            canceledOrder.getCustomerName(),
            canceledOrder.getPhone(),
            canceledOrder.getDeliveryAddress(),
            canceledOrder.getCreatedAt(),
            itemResponses
        );
    }
}
