package com.awesomepizza.order.service.api;

import java.util.Optional;

import com.awesomepizza.order.dto.CreateOrderRequest;
import com.awesomepizza.order.dto.OrderResponse;
import com.awesomepizza.order.dto.UpdateOrderRequest;

public interface ICustomerOrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    Optional<OrderResponse> getByOrderCode(String orderCode);
    OrderResponse updateOrder(String orderCode, UpdateOrderRequest request);
    OrderResponse cancelOrder(String orderCode);
}
