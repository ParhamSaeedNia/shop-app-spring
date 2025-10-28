package org.example.shopapp.cart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.shopapp.cart.dto.request.AddToCartRequest;
import org.example.shopapp.cart.dto.request.UpdateCartItemRequest;
import org.example.shopapp.cart.dto.response.CartItemResponse;
import org.example.shopapp.cart.dto.response.CartResponse;
import org.example.shopapp.common.entity.Cart;
import org.example.shopapp.common.entity.CartItem;
import org.example.shopapp.common.entity.Product;
import org.example.shopapp.common.entity.User;
import org.example.shopapp.common.exception.CartNotFoundException;
import org.example.shopapp.common.exception.ProductNotFoundException;
import org.example.shopapp.cart.repository.CartItemRepository;
import org.example.shopapp.cart.repository.CartRepository;
import org.example.shopapp.product.repository.ProductRepository;
import org.example.shopapp.product.dto.response.ProductResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    public CartResponse getCurrentUserCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserIdWithItems(currentUser.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        
        return mapToCartResponse(cart);
    }
    
    @Transactional
    public CartItemResponse addToCart(AddToCartRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));
        
        if (!product.getIsActive()) {
            throw new RuntimeException("Product is not available");
        }
        
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        
        // Get or create cart
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(currentUser)
                            .totalPrice(java.math.BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });
        
        // Check if item already exists in cart
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            
            cartItemRepository.save(cartItem);
        }
        
        // Update cart total
        cart.calculateTotalPrice();
        cartRepository.save(cart);
        
        return getCurrentUserCart().getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to add item to cart"));
    }
    
    @Transactional
    public CartItemResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to cart item");
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        
        // Update cart total
        Cart cart = cartItem.getCart();
        cart.calculateTotalPrice();
        cartRepository.save(cart);
        
        return mapToCartItemResponse(cartItem);
    }
    
    @Transactional
    public void removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        
        // Verify ownership
        User currentUser = getCurrentUser();
        if (!cartItem.getCart().getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access to cart item");
        }
        
        cartItemRepository.delete(cartItem);
        
        // Update cart total
        Cart cart = cartItem.getCart();
        cart.calculateTotalPrice();
        cartRepository.save(cart);
    }
    
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user"));
        
        cartItemRepository.deleteByCartId(cart.getId());
        
        cart.setTotalPrice(java.math.BigDecimal.ZERO);
        cartRepository.save(cart);
    }
    
    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    
    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::mapToCartItemResponse)
                .toList();
        
        return CartResponse.builder()
                .id(cart.getId())
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt())
                .cartItems(cartItemResponses)
                .build();
    }
    
    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .product(mapToProductResponse(cartItem.getProduct()))
                .build();
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
                .build();
    }
}
