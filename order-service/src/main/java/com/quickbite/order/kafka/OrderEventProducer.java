package com.quickbite.order.kafka;

import com.quickbite.common.event.OrderConfirmedEvent;
import com.quickbite.common.event.OrderPlacedEvent;
import com.quickbite.common.event.OrderPreparedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    public static final String TOPIC_ORDER_PLACED = "order-placed-topic";
    public static final String TOPIC_ORDER_CONFIRMED = "order-confirmed-topic";
    public static final String TOPIC_ORDER_PREPARED = "order-prepared-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderId: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_PLACED, String.valueOf(event.getOrderId()), event);
    }

    public void sendOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("Publishing OrderConfirmedEvent for orderId: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_CONFIRMED, String.valueOf(event.getOrderId()), event);
    }

    public void sendOrderPreparedEvent(OrderPreparedEvent event) {
        log.info("Publishing OrderPreparedEvent for orderId: {}", event.getOrderId());
        kafkaTemplate.send(TOPIC_ORDER_PREPARED, String.valueOf(event.getOrderId()), event);
    }
}
