package org.example.shopapp.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.shopapp.product.dto.response.ProductResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    
    private Long id;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String productName;
    private LocalDateTime createdAt;
    private ProductResponse product;
}
