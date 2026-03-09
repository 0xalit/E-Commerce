package com.example.e_commerce.cart;

import com.example.e_commerce.cart.dto.CartItemRequestDto;
import com.example.e_commerce.cart.dto.CartResponseDto;
import com.example.e_commerce.cart.service.CartService;
import com.example.e_commerce.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart endpoints scoped to the authenticated user.
 * userId is never trusted from the URL — always resolved from the JWT.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart() {
        return ResponseEntity.ok(cartService.getCartByUserId(securityUtils.getCurrentUserId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(@Valid @RequestBody CartItemRequestDto cartItemRequestDto) {
        return ResponseEntity.ok(cartService.addItemToCart(securityUtils.getCurrentUserId(), cartItemRequestDto));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponseDto> updateItem(@PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(securityUtils.getCurrentUserId(), itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItemFromCart(securityUtils.getCurrentUserId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(securityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
