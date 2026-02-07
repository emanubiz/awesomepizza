package com.awesomepizza.order.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.awesomepizza.order.domain.enums.OrderStatus;

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