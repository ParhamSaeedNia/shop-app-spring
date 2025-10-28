package org.example.shopapp.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.product.dto.response.CategoryResponse;
import org.example.shopapp.product.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category management endpoints")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "Get all categories", description = "Retrieves a list of all available product categories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }
    
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success("Category retrieved successfully", category));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Search categories", description = "Searches for categories by name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(
            @Parameter(description = "Category name to search for") @RequestParam String name) {
        List<CategoryResponse> categories = categoryService.searchCategories(name);
        return ResponseEntity.ok(ApiResponse.success("Search results retrieved successfully", categories));
    }
}
