package org.example.shopapp.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a product")
public class UpdateProductRequest {
    
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    @Schema(description = "Product name", example = "iPhone 15 Pro")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Product description", example = "Latest iPhone with advanced features")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    @Schema(description = "Product price", example = "999.99")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity must not be negative")
    @Schema(description = "Available stock quantity", example = "100")
    private Integer stock;
    
    @Schema(description = "Category ID", example = "1")
    private Long categoryId;
    
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    private String imageUrl;
    
    @Schema(description = "Whether the product is active", example = "true")
    private Boolean isActive;
}
