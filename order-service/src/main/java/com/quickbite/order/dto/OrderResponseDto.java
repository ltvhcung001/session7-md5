package com.quickbite.order.dto;

import com.quickbite.common.dto.OrderItemDto;
import com.quickbite.common.enums.OrderStatus;
import com.quickbite.common.enums.PaymentMethod;
import com.quickbite.common.enums.PaymentStatus;
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
public class OrderResponseDto {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String customerPhone;
    private Long restaurantId;
    private String restaurantName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;
    private String deliveryAddress;
    private String specialInstructions;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
