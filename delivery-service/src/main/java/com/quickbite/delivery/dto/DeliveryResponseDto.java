package com.quickbite.delivery.dto;

import com.quickbite.common.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDto {
    private Long id;
    private Long orderId;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Long customerId;
    private Long restaurantId;
    private String pickupAddress;
    private String deliveryAddress;
    private DeliveryStatus status;
    private Double currentLatitude;
    private Double currentLongitude;
    private Integer estimatedMinutes;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
