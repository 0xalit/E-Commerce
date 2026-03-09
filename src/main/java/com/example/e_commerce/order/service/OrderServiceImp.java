package com.example.e_commerce.order.service;

import com.example.e_commerce.cart.entity.Cart;
import com.example.e_commerce.cart.entity.CartItem;
import com.example.e_commerce.cart.repo.CartRepository;
import com.example.e_commerce.order.OrderMapper;
import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.order.dto.OrderResponseDto;
import com.example.e_commerce.order.entity.Order;
import com.example.e_commerce.order.entity.OrderItem;
import com.example.e_commerce.order.entity.OrderStatus;
import com.example.e_commerce.order.repo.OrderRepository;
import com.example.e_commerce.product.entity.Product;
import com.example.e_commerce.product.repo.ProductRepository;
import com.example.e_commerce.user.entity.Address;
import com.example.e_commerce.user.repo.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImp implements OrderService {

        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final AddressRepository addressRepository;
        private final OrderMapper orderMapper;

        @Override
        public OrderResponseDto placeOrder(Long userId, OrderRequestDto orderRequestDto) {
                // 1. Get user cart and validate it's not empty
                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

                if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
                        throw new IllegalArgumentException("Cannot place order with an empty cart");
                }

                // 2. Fetch and validate address belongs to the user
                Address address = addressRepository.findById(orderRequestDto.getAddressId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Address not found with id: " + orderRequestDto.getAddressId()));

                if (!address.getUser().getId().equals(userId)) {
                        throw new IllegalArgumentException("Address does not belong to this user");
                }

                // 3. Create order with snapshot address
                Order order = Order.builder()
                                .user(cart.getUser())
                                .status(OrderStatus.PENDING)
                                .shippingCountry(address.getCountry())
                                .shippingCity(address.getCity())
                                .shippingStreet(address.getStreet())
                                .shippingDescription(address.getDescription())
                                .orderItems(new ArrayList<>())
                                .build();

                // 4. For each cart item: deduct stock and create order item
                BigDecimal totalPrice = BigDecimal.ZERO;

                for (CartItem cartItem : cart.getCartItems()) {
                        Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Product not found with id: " + cartItem.getProduct().getId()));

                        // Validate stock
                        int newStock = product.getStockQuantity() - cartItem.getQuantity();
                        if (newStock < 0) {
                                log.warn("Insufficient stock for product id={} name='{}': available={}, requested={}",
                                                product.getId(), product.getName(),
                                                product.getStockQuantity(), cartItem.getQuantity());
                                throw new IllegalArgumentException(
                                                "Insufficient stock for product: " + product.getName()
                                                                + ". Available: " + product.getStockQuantity()
                                                                + ", Requested: " + cartItem.getQuantity());
                        }
                        product.setStockQuantity(newStock);
                        productRepository.save(product);

                        // Create order item with price snapshot
                        BigDecimal priceAtPurchase = product.getPrice();
                        BigDecimal subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                        totalPrice = totalPrice.add(subtotal);

                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .product(product)
                                        .quantity(cartItem.getQuantity())
                                        .priceAtPurchase(priceAtPurchase)
                                        .subtotal(subtotal)
                                        .build();
                        order.getOrderItems().add(orderItem);
                }

                // 5. Set total price and save
                order.setTotalPrice(totalPrice);
                Order savedOrder = orderRepository.save(order);

                log.info("Order placed: id={}, userId={}, items={}, total={}",
                                savedOrder.getId(), userId,
                                savedOrder.getOrderItems().size(), savedOrder.getTotalPrice());
                return orderMapper.toResponseDto(savedOrder);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponseDto getOrderById(Long userId, Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

                // Verify the order belongs to the requesting user
                if (!order.getUser().getId().equals(userId)) {
                        throw new AccessDeniedException("Order " + orderId + " does not belong to user " + userId);
                }

                return orderMapper.toResponseDto(order);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
                return orderRepository.findByUserId(userId, pageable)
                                .map(orderMapper::toResponseDto);
        }

        @Override
        public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus status) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
                OrderStatus previousStatus = order.getStatus();
                order.setStatus(status);
                Order updatedOrder = orderRepository.save(order);
                log.info("Order id={} status changed: {} → {}", orderId, previousStatus, status);
                return orderMapper.toResponseDto(updatedOrder);
        }
}
