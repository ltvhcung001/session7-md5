package com.quickbite.delivery.kafka;

import com.quickbite.common.event.OrderPreparedEvent;
import com.quickbite.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(topics = "order-prepared-topic", groupId = "delivery-group")
    public void consumeOrderPrepared(OrderPreparedEvent event) {
        log.info("Received OrderPreparedEvent in DeliveryService for orderId: {}", event.getOrderId());
        deliveryService.handleOrderPreparedEvent(event);
    }
}
