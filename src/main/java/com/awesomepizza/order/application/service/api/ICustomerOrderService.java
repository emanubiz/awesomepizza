package com.awesomepizza.order.application.service.api;

import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;

import java.util.Optional;

public interface ICustomerOrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    Optional<OrderResponse> getByOrderCode(String orderCode);
    OrderResponse updateOrder(String orderCode, UpdateOrderRequest request);
    OrderResponse cancelOrder(String orderCode);
}
