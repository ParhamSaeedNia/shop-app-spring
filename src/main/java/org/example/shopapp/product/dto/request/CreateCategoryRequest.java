package org.example.shopapp.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request to create a new category")
public class CreateCategoryRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Schema(description = "Category name", example = "Electronics")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Category description", example = "Electronic devices and gadgets")
    private String description;
}
