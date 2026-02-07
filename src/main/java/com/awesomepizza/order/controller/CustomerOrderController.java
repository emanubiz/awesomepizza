package com.awesomepizza.order.controller;

import com.awesomepizza.order.dto.CreateOrderRequest;
import com.awesomepizza.order.dto.OrderResponse;
import com.awesomepizza.order.dto.UpdateOrderRequest;
import com.awesomepizza.order.service.api.ICustomerOrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for customer-facing order operations.
 * This controller provides endpoints for customers to create, retrieve, update,
 * and cancel their pizza orders.
 */
@Tag(name = "Customer Orders", description = "Operations for customers to create, track, update, and cancel orders")
@Slf4j // Lombok annotation for logging
@RestController
@RequestMapping("/api/v1/orders")
public class CustomerOrderController {

    private final ICustomerOrderService customerOrderService;

    /**
     * Constructs a new CustomerOrderController with the necessary service dependency.
     *
     * @param customerOrderService The service handling customer-specific order logic.
     */
    public CustomerOrderController(ICustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    /**
     * Creates a new pizza order based on the provided request body.
     * The order will be initialized with a 'PENDING' status.
     *
     * @param request The {@link CreateOrderRequest} containing the details of the order to create.
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the newly created order
     *         with HTTP status 201 (Created).
     */
    @Operation(summary = "Create a new order", description = "Allows customers to create a new pizza order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Received request to create a new order for customer: {}", request.customerName());
        OrderResponse response = customerOrderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the details of a specific order using its unique order code.
     *
     * @param code The unique order code of the order to retrieve.
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} if the order is found
     *         with HTTP status 200 (OK), or HTTP status 404 (Not Found) if no order matches the code.
     */
    @Operation(summary = "Get order by code", description = "Retrieves the details of a specific order using its unique code.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found and returned"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{code}")
    public ResponseEntity<OrderResponse> getOrderByCode(@PathVariable String code) {
        log.debug("Received request to get order by code: {}", code);
        return customerOrderService.getByOrderCode(code)
            .map(ResponseEntity::ok)
            .orElseGet(() -> {
                log.warn("Order with code {} not found.", code);
                return ResponseEntity.notFound().build();
            });
    }

    /**
     * Updates an existing order specified by its order code with new details provided in the request body.
     * This operation is only permitted if the order's current status allows customer modifications.
     *
     * @param code    The unique order code of the order to update.
     * @param request The {@link UpdateOrderRequest} containing the updated order details.
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the updated order
     *         with HTTP status 200 (OK).
     */
    @Operation(summary = "Update an existing order", description = "Allows customers to update details of an existing order. Only allowed for certain order statuses.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or order status does not permit update"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{code}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String code, @Valid @RequestBody UpdateOrderRequest request) {
        log.info("Received request to update order with code: {}", code);
        OrderResponse response = customerOrderService.updateOrder(code, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an existing order specified by its order code.
     * This operation is only permitted if the order's current status allows customer modifications.
     * The order's status will be changed to 'CANCELED'.
     *
     * @param code The unique order code of the order to cancel.
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the canceled order
     *         with HTTP status 200 (OK).
     */
    @Operation(summary = "Cancel an order", description = "Allows customers to cancel an existing order. Only allowed for certain order statuses.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order successfully canceled"),
        @ApiResponse(responseCode = "400", description = "Order status does not permit cancellation"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{code}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String code) {
        log.info("Received request to cancel order with code: {}", code);
        OrderResponse response = customerOrderService.cancelOrder(code);
        return ResponseEntity.ok(response);
    }
}