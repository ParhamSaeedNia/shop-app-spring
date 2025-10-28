package org.example.shopapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.dto.response.CategoryResponse;
import org.example.shopapp.entity.Category;
import org.example.shopapp.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findByIdWithProducts(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return mapToCategoryResponse(category);
    }
    
    public List<CategoryResponse> searchCategories(String name) {
        List<Category> categories = categoryRepository.findByNameContaining(name);
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .toList();
    }
    
    @Transactional
    public CategoryResponse createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category with name '" + name + "' already exists");
        }
        
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();
        
        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }
    
    @Transactional
    public CategoryResponse updateCategory(Long id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setName(name);
        category.setDescription(description);
        
        category = categoryRepository.save(category);
        return mapToCategoryResponse(category);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        
        categoryRepository.deleteById(id);
    }
    
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
