package org.example.shopapp.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.product.dto.response.CategoryResponse;
import org.example.shopapp.product.dto.response.ProductResponse;
import org.example.shopapp.product.service.CategoryService;
import org.example.shopapp.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative operations for managing products and categories")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    
    // Category Management
    @Operation(summary = "Create category", description = "Creates a new product category (Admin only)")
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category description") @RequestParam(required = false) String description) {
        try {
            CategoryResponse category = categoryService.createCategory(name, description);
            return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Update category", description = "Updates an existing product category (Admin only)")
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category description") @RequestParam(required = false) String description) {
        try {
            CategoryResponse category = categoryService.updateCategory(id, name, description);
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Delete category", description = "Deletes a product category (Admin only)")
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", "Category deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // Product Management
    @Operation(summary = "Get low stock products", description = "Retrieves products with stock below threshold (Admin only)")
    @GetMapping("/products/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts(
            @Parameter(description = "Stock threshold") @RequestParam(defaultValue = "10") Integer threshold) {
        try {
            List<ProductResponse> products = productService.getLowStockProducts(threshold);
            return ResponseEntity.ok(ApiResponse.success("Low stock products retrieved successfully", products));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Get all products (Admin)", description = "Retrieves all products with admin privileges (Admin only)")
    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }
}
