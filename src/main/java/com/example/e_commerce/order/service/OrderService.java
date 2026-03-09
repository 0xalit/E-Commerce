package com.example.e_commerce.order.service;

import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.order.dto.OrderResponseDto;
import com.example.e_commerce.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto placeOrder(Long userId, OrderRequestDto orderRequestDto);

    OrderResponseDto getOrderById(Long userId, Long orderId);

    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);

    OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status);
}
