package com.awesomepizza.order.application.mapper;

import com.awesomepizza.order.application.dto.OrderItemRequest;
import com.awesomepizza.order.application.dto.OrderItemResponse;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;
import com.awesomepizza.order.domain.entity.Order;
import com.awesomepizza.order.domain.entity.OrderItem;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper component responsible for converting between Order entities and their corresponding
 * Data Transfer Objects (DTOs) for requests and responses.
 * This class facilitates the separation of concerns between domain models and API representations.
 */
@Component
public class OrderMapper {

    /**
     * Converts an {@link Order} entity into an {@link OrderResponse} DTO.
     * Includes mapping of associated {@link OrderItem} entities to {@link OrderItemResponse} DTOs.
     *
     * @param order The {@link Order} entity to convert.
     * @return An {@link OrderResponse} DTO representing the order.
     */
    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getCustomerName(),
                order.getPhone(),
                order.getDeliveryAddress(),
                order.getCreatedAt(),
                items
        );
    }

    /**
     * Converts an {@link OrderItem} entity into an {@link OrderItemResponse} DTO.
     * This is a private helper method used during the conversion of an {@link Order} to {@link OrderResponse}.
     *
     * @param item The {@link OrderItem} entity to convert.
     * @return An {@link OrderItemResponse} DTO representing the order item.
     */
    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getPizzaName(),
                item.getQuantity(),
                item.getPrice()
        );
    }

    /**
     * Converts a {@link CreateOrderRequest} DTO into an {@link Order} entity.
     * This method initializes a new Order entity with details from the request,
     * including its associated {@link OrderItem} entities.
     *
     * @param request The {@link CreateOrderRequest} DTO to convert.
     * @return An {@link Order} entity populated with data from the request.
     */
    public Order toEntity(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setPhone(request.phone());
        order.setDeliveryAddress(request.deliveryAddress());
        request.orderItems().forEach(item -> order.addOrderItem(toEntity(item)));
        return order;
    }

    /**
     * Updates an existing {@link Order} entity based on the provided {@link UpdateOrderRequest} DTO.
     * This method applies partial updates, only setting fields present in the request (using Optional).
     * It also handles updating the list of order items by clearing existing ones and adding new ones.
     *
     * @param order   The {@link Order} entity to be updated.
     * @param request The {@link UpdateOrderRequest} DTO containing the new data.
     */
    public void updateEntity(Order order, UpdateOrderRequest request) {
        request.customerName().ifPresent(order::setCustomerName);
        request.phone().ifPresent(order::setPhone);
        request.deliveryAddress().ifPresent(order::setDeliveryAddress);
        request.orderItems().ifPresent(items -> {
            order.getOrderItems().clear(); 
            items.forEach(item -> order.addOrderItem(toEntity(item))); 
        });
    }

    /**
     * Converts an {@link OrderItemRequest} DTO into an {@link OrderItem} entity.
     * This is a private helper method used when creating or updating {@link Order} entities.
     *
     * @param request The {@link OrderItemRequest} DTO to convert.
     * @return An {@link OrderItem} entity populated with data from the request.
     */
    private OrderItem toEntity(OrderItemRequest request) {
        OrderItem item = new OrderItem();
        item.setPizzaName(request.pizzaName());
        item.setQuantity(request.quantity());
        item.setPrice(request.price());
        return item;
    }
}