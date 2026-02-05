package com.awesomepizza.order.adapter.in.web;

import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.service.api.IPizzaioloOrderService;
import com.awesomepizza.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pizzaiolo Orders", description = "Operations for pizzaiolos to manage orders")
@SecurityRequirement(name = "basicAuth") // Indicate that this controller requires Basic Auth
@RestController
@RequestMapping("/api/v1/pizzaiolo/orders")
public class PizzaioloOrderController {

    private final IPizzaioloOrderService pizzaioloOrderService;

    public PizzaioloOrderController(IPizzaioloOrderService pizzaioloOrderService) {
        this.pizzaioloOrderService = pizzaioloOrderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = pizzaioloOrderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{code}/take")
    public ResponseEntity<OrderResponse> takeOrder(@PathVariable String code) {
        OrderResponse response = pizzaioloOrderService.takeOrder(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{code}/status/{newStatus}")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String code, @PathVariable String newStatus) {
        OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
        OrderResponse response = pizzaioloOrderService.updateOrderStatus(code, status);
        return ResponseEntity.ok(response);
    }
}
