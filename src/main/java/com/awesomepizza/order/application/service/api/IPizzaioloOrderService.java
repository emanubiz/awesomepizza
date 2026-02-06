package com.awesomepizza.order.application.service.api;

import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.domain.enums.OrderStatus;

import java.util.List;

public interface IPizzaioloOrderService {
    List<OrderResponse> getAllOrders();
    List<OrderResponse> getAllPendingOrders();
    OrderResponse takeOrder(String orderCode);
    OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus);
    OrderResponse takeNextOrder();
}