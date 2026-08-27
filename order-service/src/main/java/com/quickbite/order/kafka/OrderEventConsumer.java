package com.quickbite.order.kafka;

import com.quickbite.common.event.DeliveryStatusUpdatedEvent;
import com.quickbite.common.event.PaymentProcessedEvent;
import com.quickbite.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-processed-topic", groupId = "order-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent for orderId: {}", event.getOrderId());
        orderService.handlePaymentProcessed(event);
    }

    @KafkaListener(topics = "delivery-status-topic", groupId = "order-group")
    public void consumeDeliveryStatus(DeliveryStatusUpdatedEvent event) {
        log.info("Received DeliveryStatusUpdatedEvent for orderId: {}, status: {}", event.getOrderId(), event.getStatus());
        orderService.handleDeliveryStatusUpdated(event.getOrderId(), event.getStatus());
    }
}
