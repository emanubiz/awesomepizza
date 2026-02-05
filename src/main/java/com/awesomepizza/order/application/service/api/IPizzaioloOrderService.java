package com.awesomepizza.order.application.service.api;

import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.domain.OrderStatus;

import java.util.List;

public interface IPizzaioloOrderService {
    List<OrderResponse> getAllOrders();
    OrderResponse takeOrder(String orderCode);
    OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus);
}
