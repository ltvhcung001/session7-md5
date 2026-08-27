package com.quickbite.order.dto;

import com.quickbite.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDto {
    @NotNull(message = "Order status is required")
    private OrderStatus status;
}
