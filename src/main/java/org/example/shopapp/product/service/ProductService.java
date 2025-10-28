package org.example.shopapp.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.product.dto.request.CreateProductRequest;
import org.example.shopapp.product.dto.request.UpdateProductRequest;
import org.example.shopapp.product.dto.response.ProductResponse;
import org.example.shopapp.common.entity.Product;
import org.example.shopapp.common.entity.Category;
import org.example.shopapp.common.exception.ProductNotFoundException;
import org.example.shopapp.product.repository.ProductRepository;
import org.example.shopapp.product.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductResponse> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(keyword, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductResponse> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public Page<ProductResponse> getAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return products.map(this::mapToProductResponse);
    }
    
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }
    
    public List<ProductResponse> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }
    
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        
        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock");
        }
        
        product.setStock(newStock);
        productRepository.save(product);
    }
    
    @Transactional
    public void restoreStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }
    
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImageUrl())
                .isActive(request.getIsActive())
                .category(category)
                .build();
        
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }
        
        Product savedProduct = productRepository.save(product);
        return mapToProductResponse(savedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .category(mapToCategoryResponse(product.getCategory()))
                .build();
    }
    
    private org.example.shopapp.product.dto.response.CategoryResponse mapToCategoryResponse(org.example.shopapp.common.entity.Category category) {
        if (category == null) {
            return null;
        }
        
        return org.example.shopapp.product.dto.response.CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
