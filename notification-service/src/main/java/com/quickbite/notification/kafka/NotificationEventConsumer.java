package com.quickbite.notification.kafka;

import com.quickbite.common.enums.PaymentStatus;
import com.quickbite.common.event.*;
import com.quickbite.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-placed-topic", groupId = "notification-group")
    public void consumeOrderPlaced(OrderPlacedEvent event) {
        log.info("NotificationService received OrderPlacedEvent for order: {}", event.getOrderId());
        notificationService.sendNotification(
                event.getCustomerId(),
                event.getCustomerEmail(),
                event.getCustomerPhone(),
                "Order Placed Successfully",
                "Your order #" + event.getOrderId() + " at " + event.getRestaurantName() + " has been received and is awaiting payment confirmation.",
                "PUSH"
        );
    }

    @KafkaListener(topics = "payment-processed-topic", groupId = "notification-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        log.info("NotificationService received PaymentProcessedEvent for order: {}", event.getOrderId());
        if (event.getStatus() == PaymentStatus.COMPLETED) {
            notificationService.sendNotification(
                    event.getCustomerId(),
                    null,
                    null,
                    "Payment Successful",
                    "Payment of $" + event.getAmount() + " for order #" + event.getOrderId() + " was processed successfully.",
                    "EMAIL"
            );
        } else {
            notificationService.sendNotification(
                    event.getCustomerId(),
                    null,
                    null,
                    "Payment Failed",
                    "Payment of $" + event.getAmount() + " failed: " + event.getFailureReason(),
                    "EMAIL"
            );
        }
    }

    @KafkaListener(topics = "order-confirmed-topic", groupId = "notification-group")
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {
        log.info("NotificationService received OrderConfirmedEvent for order: {}", event.getOrderId());
        notificationService.sendNotification(
                event.getCustomerId(),
                null,
                null,
                "Order Confirmed",
                "Restaurant is now preparing your food for order #" + event.getOrderId(),
                "PUSH"
        );
    }

    @KafkaListener(topics = "delivery-status-topic", groupId = "notification-group")
    public void consumeDeliveryStatus(DeliveryStatusUpdatedEvent event) {
        log.info("NotificationService received DeliveryStatusUpdatedEvent for order: {}", event.getOrderId());
        notificationService.sendNotification(
                event.getCustomerId(),
                null,
                null,
                "Delivery Update: " + event.getStatus(),
                "Driver " + event.getDriverName() + " status: " + event.getStatus() + ". Notes: " + (event.getNotes() != null ? event.getNotes() : ""),
                "SMS"
        );
    }
}
