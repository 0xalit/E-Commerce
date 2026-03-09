package com.example.e_commerce.cart.service;

import com.example.e_commerce.cart.dto.CartItemRequestDto;
import com.example.e_commerce.cart.dto.CartResponseDto;

public interface CartService {

    CartResponseDto getCartByUserId(Long userId);

    CartResponseDto addItemToCart(Long userId, CartItemRequestDto cartItemRequestDto);

    CartResponseDto updateCartItem(Long userId, Long itemId, Integer quantity);

    void removeItemFromCart(Long userId, Long itemId);

    void clearCart(Long userId);
}
