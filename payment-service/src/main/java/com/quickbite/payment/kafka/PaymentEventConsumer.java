package com.quickbite.payment.kafka;

import com.quickbite.common.event.OrderPlacedEvent;
import com.quickbite.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-placed-topic", groupId = "payment-group")
    public void consumeOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent in PaymentService for orderId: {}", event.getOrderId());
        paymentService.handleOrderPlacedEvent(event);
    }
}
