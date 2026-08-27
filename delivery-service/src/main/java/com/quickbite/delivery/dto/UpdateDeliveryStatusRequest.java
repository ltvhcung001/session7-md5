package com.quickbite.delivery.dto;

import com.quickbite.common.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeliveryStatusRequest {

    @NotNull(message = "Delivery status is required")
    private DeliveryStatus status;

    private Double currentLatitude;

    private Double currentLongitude;

    private String notes;
}
