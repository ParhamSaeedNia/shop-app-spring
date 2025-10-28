package org.example.shopapp.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private Long id;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<CartItemResponse> cartItems;
}
