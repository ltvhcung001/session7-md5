package com.quickbite.order.service;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.dto.MenuItemSummaryDto;
import com.quickbite.common.dto.OrderItemDto;
import com.quickbite.common.dto.UserSummaryDto;
import com.quickbite.common.enums.DeliveryStatus;
import com.quickbite.common.enums.OrderStatus;
import com.quickbite.common.enums.PaymentStatus;
import com.quickbite.common.event.OrderConfirmedEvent;
import com.quickbite.common.event.OrderPlacedEvent;
import com.quickbite.common.event.OrderPreparedEvent;
import com.quickbite.common.event.PaymentProcessedEvent;
import com.quickbite.common.exception.BadRequestException;
import com.quickbite.common.exception.ResourceNotFoundException;
import com.quickbite.order.client.RestaurantClient;
import com.quickbite.order.client.UserClient;
import com.quickbite.order.dto.OrderItemRequestDto;
import com.quickbite.order.dto.OrderResponseDto;
import com.quickbite.order.dto.PlaceOrderRequestDto;
import com.quickbite.order.entity.Order;
import com.quickbite.order.entity.OrderItem;
import com.quickbite.order.kafka.OrderEventProducer;
import com.quickbite.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponseDto placeOrder(Long customerId, PlaceOrderRequestDto request) {
        // 1. Fetch user details (fallback gracefully if service unavailable)
        String customerEmail = "";
        String customerPhone = "";
        try {
            ApiResponse<UserSummaryDto> userRes = userClient.getUserSummary(customerId);
            if (userRes != null && userRes.getData() != null) {
                customerEmail = userRes.getData().getEmail();
                customerPhone = userRes.getData().getPhoneNumber();
            }
        } catch (Exception e) {
            log.warn("Could not fetch user details from auth-user-service: {}", e.getMessage());
        }

        // 2. Validate Restaurant and fetch info
        String restaurantName = "Restaurant #" + request.getRestaurantId();
        try {
            ApiResponse<?> restRes = restaurantClient.getRestaurantById(request.getRestaurantId());
            if (restRes != null && restRes.getData() instanceof java.util.Map<?, ?> map) {
                Object nameVal = map.get("name");
                if (nameVal != null) {
                    restaurantName = nameVal.toString();
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch restaurant details: {}", e.getMessage());
        }

        // 3. Build items and calculate totals
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemReq : request.getItems()) {
            BigDecimal unitPrice = BigDecimal.valueOf(10.00); // Default fallback
            String itemName = "Item #" + itemReq.getMenuItemId();

            try {
                ApiResponse<MenuItemSummaryDto> itemRes = restaurantClient.getMenuItemSummary(itemReq.getMenuItemId());
                if (itemRes != null && itemRes.getData() != null) {
                    MenuItemSummaryDto itemDto = itemRes.getData();
                    if (!itemDto.getIsAvailable()) {
                        throw new BadRequestException("Item '" + itemDto.getName() + "' is currently unavailable");
                    }
                    unitPrice = itemDto.getPrice();
                    itemName = itemDto.getName();
                }
            } catch (BadRequestException bre) {
                throw bre;
            } catch (Exception e) {
                log.warn("Could not verify menu item {}: {}", itemReq.getMenuItemId(), e.getMessage());
            }

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(itemReq.getMenuItemId())
                    .itemName(itemName)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            orderItems.add(orderItem);
        }

        BigDecimal deliveryFee = BigDecimal.valueOf(2.50);
        BigDecimal finalAmount = totalAmount.add(deliveryFee);

        Order order = Order.builder()
                .orderNumber("QB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerId(customerId)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .restaurantId(request.getRestaurantId())
                .restaurantName(restaurantName)
                .status(OrderStatus.PENDING_PAYMENT)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryFee(deliveryFee)
                .finalAmount(finalAmount)
                .deliveryAddress(request.getDeliveryAddress())
                .specialInstructions(request.getSpecialInstructions())
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // 4. Publish OrderPlacedEvent via Kafka
        List<OrderItemDto> itemDtos = savedOrder.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .customerEmail(savedOrder.getCustomerEmail())
                .customerPhone(savedOrder.getCustomerPhone())
                .restaurantId(savedOrder.getRestaurantId())
                .restaurantName(savedOrder.getRestaurantName())
                .totalAmount(savedOrder.getFinalAmount())
                .paymentMethod(savedOrder.getPaymentMethod())
                .deliveryAddress(savedOrder.getDeliveryAddress())
                .items(itemDtos)
                .timestamp(LocalDateTime.now())
                .build();

        orderEventProducer.sendOrderPlacedEvent(event);

        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);

        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            OrderPreparedEvent event = OrderPreparedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(updated.getId())
                    .restaurantId(updated.getRestaurantId())
                    .restaurantName(updated.getRestaurantName())
                    .restaurantAddress(updated.getDeliveryAddress())
                    .customerId(updated.getCustomerId())
                    .deliveryAddress(updated.getDeliveryAddress())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderEventProducer.sendOrderPreparedEvent(event);
        }

        return mapToDto(updated);
    }

    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Processing PaymentProcessedEvent for orderId: {}, status: {}", event.getOrderId(), event.getStatus());
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (event.getStatus() == PaymentStatus.COMPLETED) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                OrderConfirmedEvent confirmedEvent = OrderConfirmedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .restaurantId(order.getRestaurantId())
                        .totalAmount(order.getFinalAmount())
                        .timestamp(LocalDateTime.now())
                        .build();
                orderEventProducer.sendOrderConfirmedEvent(confirmedEvent);
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);
            }
        });
    }

    @Transactional
    public void handleDeliveryStatusUpdated(Long orderId, DeliveryStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            if (status == DeliveryStatus.OUT_FOR_DELIVERY) {
                order.setStatus(OrderStatus.DELIVERING);
            } else if (status == DeliveryStatus.DELIVERED) {
                order.setStatus(OrderStatus.DELIVERED);
            }
            orderRepository.save(order);
        });
    }

    private OrderResponseDto mapToDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems() != null
                ? order.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .menuItemId(i.getMenuItemId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .restaurantId(order.getRestaurantId())
                .restaurantName(order.getRestaurantName())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .finalAmount(order.getFinalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .specialInstructions(order.getSpecialInstructions())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
