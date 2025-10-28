package org.example.shopapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.dto.request.CreateOrderRequest;
import org.example.shopapp.dto.response.OrderResponse;
import org.example.shopapp.entity.*;
import org.example.shopapp.exception.CartNotFoundException;
import org.example.shopapp.exception.OrderNotFoundException;
import org.example.shopapp.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final ShipmentRepository shipmentRepository;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserIdWithItems(currentUser.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Create order
        Order order = Order.builder()
                .user(currentUser)
                .totalPrice(cart.getTotalPrice())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .notes(request.getNotes())
                .build();
        
        order = orderRepository.save(order);
        
        // Create order items and update stock
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            
            // Check stock availability
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Create order item
            OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .productName(product.getName())
                    .build();
            
            // Update product stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }
        
        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalPrice())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.CASH_ON_DELIVERY) // Default
                .build();
        
        paymentRepository.save(payment);
        
        // Create shipment record
        Shipment shipment = Shipment.builder()
                .order(order)
                .status(Shipment.ShipmentStatus.PENDING)
                .courier("Default Courier")
                .shippingAddress(request.getShippingAddress())
                .build();
        
        shipmentRepository.save(shipment);
        
        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        
        return mapToOrderResponse(order);
    }
    
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithPaymentAndShipment(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        
        return mapToOrderResponse(order);
    }
    
    public Page<OrderResponse> getUserOrders(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Order> orders = orderRepository.findByUserId(currentUser.getId(), pageable);
        return orders.map(this::mapToOrderResponse);
    }
    
    public List<OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .toList();
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }
    
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to order");
        }
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be cancelled");
        }
        
        // Restore stock
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        
        return mapToOrderResponse(order);
    }
    
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    private OrderResponse mapToOrderResponse(Order order) {
        List<org.example.shopapp.dto.response.OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList();
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .orderItems(orderItemResponses)
                .payment(mapToPaymentResponse(order.getPayment()))
                .shipment(mapToShipmentResponse(order.getShipment()))
                .build();
    }
    
    private org.example.shopapp.dto.response.OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return org.example.shopapp.dto.response.OrderItemResponse.builder()
                .id(orderItem.getId())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .totalPrice(orderItem.getTotalPrice())
                .productName(orderItem.getProductName())
                .createdAt(orderItem.getCreatedAt())
                .product(mapToProductResponse(orderItem.getProduct()))
                .build();
    }
    
    private org.example.shopapp.dto.response.PaymentResponse mapToPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        return org.example.shopapp.dto.response.PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .createdAt(payment.getCreatedAt())
                .build();
    }
    
    private org.example.shopapp.dto.response.ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        
        return org.example.shopapp.dto.response.ShipmentResponse.builder()
                .id(shipment.getId())
                .status(shipment.getStatus())
                .courier(shipment.getCourier())
                .trackingNumber(shipment.getTrackingNumber())
                .estimatedDeliveryDate(shipment.getEstimatedDeliveryDate())
                .actualDeliveryDate(shipment.getActualDeliveryDate())
                .shippingAddress(shipment.getShippingAddress())
                .notes(shipment.getNotes())
                .createdAt(shipment.getCreatedAt())
                .build();
    }
    
    private org.example.shopapp.dto.response.ProductResponse mapToProductResponse(Product product) {
        if (product == null) {
            return null;
        }
        
        return org.example.shopapp.dto.response.ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
