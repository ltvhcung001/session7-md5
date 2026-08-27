package com.quickbite.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreparedEvent {
    private String eventId;
    private Long orderId;
    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;
    private Long customerId;
    private String deliveryAddress;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
