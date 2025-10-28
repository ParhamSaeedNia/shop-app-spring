package org.example.shopapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.dto.request.PaymentRequest;
import org.example.shopapp.dto.response.PaymentResponse;
import org.example.shopapp.entity.Order;
import org.example.shopapp.entity.Payment;
import org.example.shopapp.entity.User;
import org.example.shopapp.exception.InvalidPaymentException;
import org.example.shopapp.exception.OrderNotFoundException;
import org.example.shopapp.repository.OrderRepository;
import org.example.shopapp.repository.PaymentRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + request.getOrderId()));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new InvalidPaymentException("Order is not in pending status");
        }
        
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
        
        // Simulate payment processing
        String transactionId = processPaymentGateway(request, payment.getAmount());
        
        if (transactionId != null) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(transactionId);
            payment.setGatewayResponse("Payment successful");
            
            // Update order status
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setGatewayResponse("Payment failed");
        }
        
        payment = paymentRepository.save(payment);
        
        return mapToPaymentResponse(payment);
    }
    
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order id: " + orderId));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        Order order = payment.getOrder();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to payment");
        }
        
        return mapToPaymentResponse(payment);
    }
    
    public List<PaymentResponse> getUserPayments() {
        User currentUser = getCurrentUser();
        List<Payment> payments = paymentRepository.findByUserId(currentUser.getId());
        return payments.stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }
    
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException("Only completed payments can be refunded");
        }
        
        // Simulate refund processing
        String refundTransactionId = processRefund(payment.getTransactionId(), payment.getAmount());
        
        if (refundTransactionId != null) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            payment.setGatewayResponse("Refund successful: " + refundTransactionId);
        } else {
            throw new InvalidPaymentException("Refund failed");
        }
        
        payment = paymentRepository.save(payment);
        
        return mapToPaymentResponse(payment);
    }
    
    private String processPaymentGateway(PaymentRequest request, java.math.BigDecimal amount) {
        // Simulate payment gateway processing
        // In a real application, you would integrate with actual payment gateways like Stripe, PayPal, etc.
        
        try {
            // Simulate processing delay
            Thread.sleep(1000);
            
            // Simulate success/failure based on some criteria
            if (request.getPaymentMethod() == Payment.PaymentMethod.CASH_ON_DELIVERY) {
                return "COD-" + UUID.randomUUID().toString().substring(0, 8);
            } else if (request.getCardNumber() != null && request.getCardNumber().length() >= 16) {
                return "CARD-" + UUID.randomUUID().toString().substring(0, 8);
            } else {
                return null; // Payment failed
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    private String processRefund(String originalTransactionId, java.math.BigDecimal amount) {
        // Simulate refund processing
        try {
            Thread.sleep(500);
            return "REFUND-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
    
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
