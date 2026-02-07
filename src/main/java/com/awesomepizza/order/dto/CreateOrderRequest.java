package com.awesomepizza.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Il nome è obbligatorio")
    @Size(min = 2, max = 100, message = "Nome tra 2 e 100 caratteri")
    String customerName,

    @NotBlank(message = "Il telefono è obbligatorio")
    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "Formato telefono non valido")
    String phone,

    @NotBlank(message = "L'indirizzo di consegna è obbligatorio")
    @Size(max = 200, message = "Indirizzo massimo 200 caratteri")
    String deliveryAddress,

    @NotEmpty(message = "L'ordine deve contenere almeno un articolo")
    List<@Valid OrderItemRequest> orderItems
) {}