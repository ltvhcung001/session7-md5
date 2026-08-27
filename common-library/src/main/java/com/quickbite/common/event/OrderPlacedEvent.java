package com.quickbite.common.event;

import com.quickbite.common.dto.OrderItemDto;
import com.quickbite.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private String eventId;
    private Long orderId;
    private Long customerId;
    private String customerEmail;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String deliveryAddress;
    private List<OrderItemDto> items;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
