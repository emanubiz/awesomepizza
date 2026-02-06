package com.awesomepizza.order.adapter.in.web;

import com.awesomepizza.order.application.dto.OrderResponse;
import com.awesomepizza.order.application.service.api.IPizzaioloOrderService;
import com.awesomepizza.order.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for pizzaiolo-specific order management operations.
 * This controller exposes endpoints that allow pizzaiolos to view, take,
 * and update the status of orders.
 *
 * All endpoints in this controller require Basic Authentication.
 */
@Tag(name = "Pizzaiolo Orders", description = "Operations for pizzaiolos to manage orders")
@SecurityRequirement(name = "basicAuth") // Indicate that this controller requires Basic Auth
@Slf4j // Lombok annotation for logging
@RestController
@RequestMapping("/api/v1/pizzaiolo/orders")
public class PizzaioloOrderController {

    private final IPizzaioloOrderService pizzaioloOrderService;

    /**
     * Constructs a new PizzaioloOrderController with the necessary service
     * dependency.
     *
     * @param pizzaioloOrderService The service handling pizzaiolo-specific order
     *                              logic.
     */
    public PizzaioloOrderController(IPizzaioloOrderService pizzaioloOrderService) {
        this.pizzaioloOrderService = pizzaioloOrderService;
    }

    /**
     * Retrieves a list of all pizza orders.
     * Orders are typically sorted by their creation date.
     *
     * @return A {@link ResponseEntity} containing a list of {@link OrderResponse}
     *         DTOs.
     */
    @Operation(summary = "Get all orders", description = "Retrieves a list of all pizza orders, ordered by creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Basic authentication required")
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.debug("Received request to get all orders for pizzaiolo");
        List<OrderResponse> orders = pizzaioloOrderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    

    /**
     * Retrieves a list of all pending pizza orders.
     * Returns only orders with PENDING status, typically sorted by their creation
     * date.
     *
     * @return A {@link ResponseEntity} containing a list of {@link OrderResponse}
     *         DTOs with PENDING status.
     */
    @Operation(summary = "Get all pending orders", description = "Retrieves a list of all pizza orders with PENDING status, ordered by creation date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of pending orders"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Basic authentication required")
    })
    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponse>> getAllPendingOrders() {
        log.debug("Received request to get all pending orders for pizzaiolo");
        List<OrderResponse> pendingOrders = pizzaioloOrderService.getAllPendingOrders();
        return ResponseEntity.ok(pendingOrders);
    }

    /**
     * Allows a pizzaiolo to take a specific order for preparation.
     * The order's status will be changed from PENDING to IN_PREPARATION.
     * This operation is typically restricted to one order in preparation at a time.
     *
     * @param code The unique order code of the order to be taken.
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the
     *         taken order.
     */
    @Operation(summary = "Take a specific order", description = "Changes the status of a PENDING order to IN_PREPARATION. Only one order can be in preparation at a time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully taken and status updated to IN_PREPARATION"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., another order is in preparation, or order status is not PENDING)"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Basic authentication required")
    })
    @PostMapping("/{code}/take")
    public ResponseEntity<OrderResponse> takeOrder(@PathVariable String code) {
        log.info("Received request to take order with code: {}", code);
        OrderResponse response = pizzaioloOrderService.takeOrder(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Allows a pizzaiolo to take the next available pending order.
     * The "next" order is the oldest order (by creation date) that is in 'PENDING'
     * status.
     * This operation is typically restricted to one order in preparation at a time.
     *
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the
     *         taken order.
     */
    @Operation(summary = "Take the next pending order", description = "Finds the oldest PENDING order and changes its status to IN_PREPARATION. Only one order can be in preparation at a time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Next pending order successfully taken and status updated to IN_PREPARATION"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., another order is in preparation)"),
            @ApiResponse(responseCode = "404", description = "No pending orders found to be taken"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Basic authentication required")
    })
    @PostMapping("/takeNext")
    public ResponseEntity<OrderResponse> takeNextOrder() {
        log.info("Received request to take the next pending order");
        OrderResponse response = pizzaioloOrderService.takeNextOrder();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the status of a specific order.
     * The new status must be a valid {@link OrderStatus} enum value.
     *
     * @param code      The unique order code of the order to be updated.
     * @param newStatus The new status to set for the order (e.g., "IN_PREPARATION",
     *                  "READY", "DELIVERED").
     * @return A {@link ResponseEntity} containing the {@link OrderResponse} of the
     *         updated order.
     */
    @Operation(summary = "Update order status", description = "Changes the status of an order to a specified new status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g., invalid status transition, or invalid newStatus value)"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Basic authentication required")
    })
    @PostMapping("/{code}/status/{newStatus}")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable String code, @PathVariable String newStatus) {
        log.info("Received request to update status for order {} to {}", code, newStatus);
        OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
        OrderResponse response = pizzaioloOrderService.updateOrderStatus(code, status);
        return ResponseEntity.ok(response);
    }
}