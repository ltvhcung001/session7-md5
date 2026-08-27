package com.quickbite.order.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.order.dto.OrderResponseDto;
import com.quickbite.order.dto.PlaceOrderRequestDto;
import com.quickbite.order.dto.UpdateOrderStatusDto;
import com.quickbite.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for placing and managing food orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new food order")
    public ResponseEntity<ApiResponse<OrderResponseDto>> placeOrder(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long customerId,
            @Valid @RequestBody PlaceOrderRequestDto request) {
        OrderResponseDto response = orderService.placeOrder(customerId, request);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/me")
    @Operation(summary = "Get orders history of the current customer")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getCustomerOrders(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long customerId) {
        List<OrderResponseDto> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get orders for a restaurant")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getRestaurantOrders(@PathVariable Long restaurantId) {
        List<OrderResponseDto> orders = orderService.getOrdersByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status (e.g. PREPARING, READY_FOR_PICKUP, CANCELLED)")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDto statusDto) {
        OrderResponseDto response = orderService.updateOrderStatus(id, statusDto.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated", response));
    }
}
