package com.awesomepizza.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    String pizzaName,
    int quantity,
    BigDecimal price
) {}
