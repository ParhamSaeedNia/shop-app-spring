package org.example.shopapp.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shopapp.common.entity.Shipment.ShipmentStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    
    private Long id;
    private ShipmentStatus status;
    private String courier;
    private String trackingNumber;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
}
