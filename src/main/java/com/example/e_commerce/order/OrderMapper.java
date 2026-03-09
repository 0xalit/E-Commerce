package com.example.e_commerce.order;

import com.example.e_commerce.order.dto.OrderItemResponseDto;
import com.example.e_commerce.order.dto.OrderResponseDto;
import com.example.e_commerce.order.entity.Order;
import com.example.e_commerce.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // 'subtotal' is computed (priceAtPurchase × qty) and stored directly on
    // OrderItem
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "orderItems")
    OrderResponseDto toResponseDto(Order order);
}
