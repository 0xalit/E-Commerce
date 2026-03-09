package com.example.e_commerce.cart;

import com.example.e_commerce.cart.dto.CartItemResponseDto;
import com.example.e_commerce.cart.dto.CartResponseDto;
import com.example.e_commerce.cart.entity.Cart;
import com.example.e_commerce.cart.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    // 'cart' back-reference on CartItem is skipped in source traversal by MapStruct
    // 'subtotal' is computed (price × qty) and stored directly on CartItem
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productPrice", source = "product.price")
    CartItemResponseDto toCartItemResponseDto(CartItem cartItem);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    CartResponseDto toResponseDto(Cart cart);
}
