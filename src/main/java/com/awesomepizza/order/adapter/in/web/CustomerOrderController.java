package com.awesomepizza.order.adapter.in.web;

import com.awesomepizza.order.application.dto.CreateOrderRequest;
import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.dto.UpdateOrderRequest;
import com.awesomepizza.order.application.service.api.ICustomerOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Customer Orders", description = "Operations for customers to create, track, update, and cancel orders")
@RestController
@RequestMapping("/api/v1/orders")
public class CustomerOrderController {

    private final ICustomerOrderService customerOrderService;

    public CustomerOrderController(ICustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = customerOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
    public ResponseEntity<OrderResponse> getOrderByCode(@PathVariable String code) {
        return customerOrderService.getByOrderCode(code)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{code}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String code, @Valid @RequestBody UpdateOrderRequest request) {
        OrderResponse response = customerOrderService.updateOrder(code, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{code}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String code) {
        OrderResponse response = customerOrderService.cancelOrder(code);
        return ResponseEntity.ok(response);
    }
}