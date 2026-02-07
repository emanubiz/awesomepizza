package com.awesomepizza.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Optional;

public record UpdateOrderRequest(
    Optional<@Size(min = 2, max = 100) String> customerName,
    Optional<@Pattern(regexp = "^\\+?\\d{9,15}$", message = "Formato telefono non valido") String> phone,
    Optional<@Size(max = 200) String> deliveryAddress,
    Optional<@Valid List<OrderItemRequest>> orderItems
) {}
