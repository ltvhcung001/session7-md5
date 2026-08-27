package com.quickbite.common.event;

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
public class DeliveryStatusUpdatedEvent {
    private String eventId;
    private Long deliveryId;
    private Long orderId;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Long customerId;
    private DeliveryStatus status;
    private String notes;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
