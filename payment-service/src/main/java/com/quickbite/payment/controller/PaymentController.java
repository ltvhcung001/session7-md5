package com.quickbite.payment.controller;

import com.quickbite.common.dto.ApiResponse;
import com.quickbite.common.security.SecurityConstants;
import com.quickbite.payment.dto.PaymentResponseDto;
import com.quickbite.payment.dto.ProcessPaymentRequest;
import com.quickbite.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Endpoints for processing and querying payment transactions")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Explicitly process a payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponseDto response = paymentService.processPayment(request);
        return new ResponseEntity<>(ApiResponse.success("Payment processed", response), HttpStatus.CREATED);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment transaction details by order ID")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentResponseDto response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/customer/me")
    @Operation(summary = "Get payment history of the current customer")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getCustomerPayments(
            @RequestHeader(value = SecurityConstants.USER_ID_HEADER, required = false, defaultValue = "1") Long customerId) {
        List<PaymentResponseDto> payments = paymentService.getCustomerPayments(customerId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }
}
