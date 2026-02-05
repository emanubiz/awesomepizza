package com.awesomepizza.order.application.dto;

import jakarta.validation.constraints.Size;
import java.util.Optional;

public record UpdateOrderRequest(
    Optional<@Size(min = 2, max = 100) String> customerName,
    Optional<@Size(min = 5, max = 20) String> phone,
    Optional<@Size(min = 5, max = 200) String> deliveryAddress
) {}
