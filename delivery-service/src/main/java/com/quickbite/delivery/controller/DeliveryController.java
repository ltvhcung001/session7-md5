package com.quickbite.delivery.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.delivery.dto.DeliveryResponseDto;
import com.quickbite.delivery.dto.UpdateDeliveryStatusRequest;
import com.quickbite.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Endpoints for driver assignments and tracking deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get delivery status and driver location for an order")
    public ResponseEntity<ApiResponse<DeliveryResponseDto>> getDeliveryByOrderId(@PathVariable Long orderId) {
        DeliveryResponseDto response = deliveryService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{deliveryId}/status")
    @Operation(summary = "Update delivery status and driver coordinates (Driver)")
    public ResponseEntity<ApiResponse<DeliveryResponseDto>> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        DeliveryResponseDto response = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success("Delivery status updated", response));
    }

    @GetMapping("/driver/me")
    @Operation(summary = "Get active and completed deliveries for the logged-in driver")
    public ResponseEntity<ApiResponse<List<DeliveryResponseDto>>> getDriverDeliveries(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "101") Long driverId) {
        List<DeliveryResponseDto> deliveries = deliveryService.getDeliveriesByDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
}
