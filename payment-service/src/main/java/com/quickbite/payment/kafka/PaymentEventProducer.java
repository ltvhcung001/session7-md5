package com.quickbite.payment.kafka;

import com.quickbite.common.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    public static final String TOPIC_PAYMENT_PROCESSED = "payment-processed-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent for orderId: {}, status: {}", event.getOrderId(), event.getStatus());
        kafkaTemplate.send(TOPIC_PAYMENT_PROCESSED, String.valueOf(event.getOrderId()), event);
    }
}
