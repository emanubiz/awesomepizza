package com.awesomepizza.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OrderItemRequest(
    @NotBlank(message = "Il nome della pizza è obbligatorio")
    @Size(min = 2, max = 100, message = "Nome pizza tra 2 e 100 caratteri")
    String pizzaName,

    @NotNull(message = "La quantità è obbligatoria")
    @Min(value = 1, message = "La quantità deve essere almeno 1")
    int quantity,

    @NotNull(message = "Il prezzo è obbligatorio")
    @DecimalMin(value = "0.01", message = "Il prezzo deve essere maggiore di 0")
    BigDecimal price
) {}
