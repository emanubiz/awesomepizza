package com.awesomepizza.order.application.dto;

import com.awesomepizza.order.domain.OrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String orderCode,
    OrderStatus status,
    String customerName,
    String phone,
    String deliveryAddress,
    Instant createdAt,
    List<OrderItemResponse> orderItems
) {} 