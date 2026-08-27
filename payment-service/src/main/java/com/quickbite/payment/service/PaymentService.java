package com.quickbite.payment.service;

import com.quickbite.common.enums.PaymentMethod;
import com.quickbite.common.enums.PaymentStatus;
import com.quickbite.common.event.OrderPlacedEvent;
import com.quickbite.common.event.PaymentProcessedEvent;
import com.quickbite.common.exception.ResourceNotFoundException;
import com.quickbite.payment.dto.PaymentResponseDto;
import com.quickbite.payment.dto.ProcessPaymentRequest;
import com.quickbite.payment.entity.Payment;
import com.quickbite.payment.kafka.PaymentEventProducer;
import com.quickbite.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional
    public PaymentResponseDto processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for orderId: {}, amount: {}", request.getOrderId(), request.getAmount());

        // Simulate payment processing
        boolean isSuccess = true; // Simulating successful payment gateway response
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String failureReason = null;

        // Simulate rare payment failure for testing if needed
        if (request.getAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
            isSuccess = false;
            failureReason = "Transaction limit exceeded";
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(isSuccess ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .transactionId(isSuccess ? transactionId : null)
                .failureReason(failureReason)
                .build();

        Payment saved = paymentRepository.save(payment);

        // Publish event to Kafka
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .customerId(saved.getCustomerId())
                .amount(saved.getAmount())
                .paymentMethod(saved.getPaymentMethod())
                .status(saved.getStatus())
                .transactionId(saved.getTransactionId())
                .failureReason(saved.getFailureReason())
                .timestamp(LocalDateTime.now())
                .build();

        paymentEventProducer.sendPaymentProcessedEvent(event);

        return mapToDto(saved);
    }

    @Transactional
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Handling OrderPlacedEvent for orderId: {}", event.getOrderId());

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .paymentMethod(event.getPaymentMethod() != null ? event.getPaymentMethod() : PaymentMethod.CREDIT_CARD)
                .build();

        processPayment(request);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getCustomerPayments(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PaymentResponseDto mapToDto(Payment p) {
        return PaymentResponseDto.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .customerId(p.getCustomerId())
                .amount(p.getAmount())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
