package org.example.shopapp.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;
    
    @Size(max = 500, message = "Billing address must not exceed 500 characters")
    private String billingAddress;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
