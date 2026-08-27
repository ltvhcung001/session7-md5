package com.quickbite.delivery.kafka;

import com.quickbite.common.event.DeliveryStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventProducer {

    public static final String TOPIC_DELIVERY_STATUS = "delivery-status-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeliveryStatusUpdatedEvent(DeliveryStatusUpdatedEvent event) {
        log.info("Publishing DeliveryStatusUpdatedEvent for orderId: {}, status: {}", event.getOrderId(), event.getStatus());
        kafkaTemplate.send(TOPIC_DELIVERY_STATUS, String.valueOf(event.getOrderId()), event);
    }
}
