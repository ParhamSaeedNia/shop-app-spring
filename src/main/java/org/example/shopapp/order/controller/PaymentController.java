package org.example.shopapp.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.order.dto.request.PaymentRequest;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.order.dto.response.PaymentResponse;
import org.example.shopapp.order.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Operation(summary = "Process payment", description = "Processes a payment for an order")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse payment = paymentService.processPayment(request);
            return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get payment by order ID", description = "Retrieves payment information for a specific order")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        try {
            PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get user payments", description = "Retrieves all payments for the current user")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getUserPayments() {
        try {
            List<PaymentResponse> payments = paymentService.getUserPayments();
            return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Refund payment", description = "Processes a refund for a payment")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @Parameter(description = "Payment ID") @PathVariable Long paymentId) {
        try {
            PaymentResponse payment = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
