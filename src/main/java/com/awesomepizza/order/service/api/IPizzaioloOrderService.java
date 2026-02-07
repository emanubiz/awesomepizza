package com.awesomepizza.order.service.api;

import com.awesomepizza.order.domain.enums.OrderStatus;
import com.awesomepizza.order.dto.OrderResponse;

import java.util.List;

public interface IPizzaioloOrderService {
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getAllPendingOrders();
    OrderResponse takeOrder(String orderCode);
    OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus);
    OrderResponse takeNextOrder();
}