package org.example.shopapp.cart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopapp.cart.dto.request.AddToCartRequest;
import org.example.shopapp.cart.dto.request.UpdateCartItemRequest;
import org.example.shopapp.common.dto.response.ApiResponse;
import org.example.shopapp.cart.dto.response.CartItemResponse;
import org.example.shopapp.cart.dto.response.CartResponse;
import org.example.shopapp.cart.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CartController {
    
    private final CartService cartService;
    
    @Operation(summary = "Get current user cart", description = "Retrieves the current user's shopping cart with all items")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentUserCart() {
        try {
            CartResponse cart = cartService.getCurrentUserCart();
            return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Operation(summary = "Add item to cart", description = "Adds a product to the user's shopping cart")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        try {
            CartItemResponse cartItem = cartService.addToCart(request);
            return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cartItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        try {
            CartItemResponse cartItem = cartService.updateCartItem(cartItemId, request);
            return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", cartItem));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(@PathVariable Long cartItemId) {
        try {
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully", "Item removed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart() {
        try {
            cartService.clearCart();
            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", "Cart cleared"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
