package org.example.shopapp.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shopapp.common.entity.Order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingAddress;
    private String billingAddress;
    private String notes;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;
    private PaymentResponse payment;
    private ShipmentResponse shipment;
}
