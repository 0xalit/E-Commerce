package com.example.e_commerce.order;

import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.order.dto.OrderResponseDto;
import com.example.e_commerce.order.entity.OrderStatus;
import com.example.e_commerce.order.service.OrderService;
import com.example.e_commerce.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Order endpoints scoped to the authenticated user.
 * userId is never trusted from the URL — always resolved from the JWT.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    /** Place an order for the currently authenticated user. */
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(orderService.placeOrder(securityUtils.getCurrentUserId(), orderRequestDto));
    }

    /**
     * Get a specific order by ID.
     * Service verifies the order belongs to the authenticated user (or user is
     * ADMIN).
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(securityUtils.getCurrentUserId(), orderId));
    }

    /** Get all orders for the currently authenticated user. */
    @GetMapping
    public ResponseEntity<Page<OrderResponseDto>> getMyOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(securityUtils.getCurrentUserId(), pageable));
    }

    /** Admin only: update an order's status. */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(@PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    /** Admin only: get orders for any user by their ID. */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUserId(@PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }
}
