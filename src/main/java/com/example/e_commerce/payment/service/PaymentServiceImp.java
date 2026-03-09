package com.example.e_commerce.payment.service;

import com.example.e_commerce.cart.repo.CartRepository;
import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.order.dto.OrderResponseDto;
import com.example.e_commerce.order.entity.Order;
import com.example.e_commerce.order.entity.OrderItem;
import com.example.e_commerce.order.entity.OrderStatus;
import com.example.e_commerce.order.repo.OrderRepository;
import com.example.e_commerce.order.service.OrderService;
import com.example.e_commerce.payment.PaymentMapper;
import com.example.e_commerce.payment.dto.PaymentResponseDto;
import com.example.e_commerce.payment.entity.Payment;
import com.example.e_commerce.payment.entity.PaymentStatus;
import com.example.e_commerce.payment.repo.PaymentRepository;
import com.example.e_commerce.product.entity.Product;
import com.example.e_commerce.product.repo.ProductRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImp implements PaymentService {

        private final PaymentRepository paymentRepository;
        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final OrderService orderService;
        private final PaymentMapper paymentMapper;

        @Override
        @Transactional(rollbackFor = Throwable.class)
        public PaymentResponseDto createPaymentIntent(Long userId, OrderRequestDto orderRequestDto) {
                // 1. Place order — reserves stock, creates order as PENDING
                OrderResponseDto orderResponse = orderService.placeOrder(userId, orderRequestDto);

                Order order = orderRepository.findById(orderResponse.getId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Order not found with id: " + orderResponse.getId()));

                // 2. Create Stripe PaymentIntent
                // Stripe expects amount in the smallest currency unit (cents for USD)
                long amountInCents = order.getTotalPrice()
                                .multiply(BigDecimal.valueOf(100))
                                .longValue();

                try {
                        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                                        .setAmount(amountInCents)
                                        .setCurrency("usd")
                                        .putMetadata("orderId", order.getId().toString())
                                        .putMetadata("userId", userId.toString())
                                        .build();

                        PaymentIntent stripeIntent = PaymentIntent.create(params);

                        // 3. Save Payment entity
                        Payment payment = Payment.builder()
                                        .order(order)
                                        .stripePaymentIntentId(stripeIntent.getId())
                                        .amount(order.getTotalPrice())
                                        .currency("usd")
                                        .status(PaymentStatus.PENDING)
                                        .build();

                        Payment savedPayment = paymentRepository.save(payment);

                        log.info("PaymentIntent created: stripeId={}, orderId={}, amount={}",
                                        stripeIntent.getId(), order.getId(), order.getTotalPrice());

                        // 4. Build response with clientSecret
                        PaymentResponseDto responseDto = paymentMapper.toResponseDto(savedPayment);
                        responseDto.setClientSecret(stripeIntent.getClientSecret());
                        return responseDto;

                } catch (StripeException e) {
                        log.error("Stripe PaymentIntent creation failed for orderId={}: {}",
                                        order.getId(), e.getMessage());
                        restoreStockForOrder(order);
                        order.setStatus(OrderStatus.PAYMENT_FAILED);
                        orderRepository.save(order);
                        throw new RuntimeException("Payment processing failed: " + e.getMessage(), e);
                }
        }

        @Override
        public void handlePaymentSuccess(String paymentIntentId) {
                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Payment not found for Stripe intent: " + paymentIntentId));

                if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        log.info("Payment already completed for intent={}, skipping", paymentIntentId);
                        return; // Idempotent — webhook may fire more than once
                }

                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);

                // Confirm the order
                Order order = payment.getOrder();
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);

                // Clear the user's cart
                cartRepository.findByUserId(order.getUser().getId())
                                .ifPresent(cart -> {
                                        cart.getCartItems().clear();
                                        cartRepository.save(cart);
                                });

                log.info("Payment succeeded: intent={}, orderId={}", paymentIntentId, order.getId());
        }

        @Override
        public void handlePaymentFailure(String paymentIntentId) {
                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Payment not found for Stripe intent: " + paymentIntentId));

                if (payment.getStatus() == PaymentStatus.FAILED) {
                        log.info("Payment already marked failed for intent={}, skipping", paymentIntentId);
                        return;
                }

                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                // Restore stock and mark order failed
                Order order = payment.getOrder();
                restoreStockForOrder(order);
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepository.save(order);

                log.info("Payment failed: intent={}, orderId={}, stock restored",
                                paymentIntentId, order.getId());
        }

        @Override
        @Transactional(readOnly = true)
        public PaymentResponseDto getPaymentByOrderId(Long userId, Long orderId) {
                Payment payment = paymentRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Payment not found for order: " + orderId));

                // Verify the order belongs to the requesting user
                if (!payment.getOrder().getUser().getId().equals(userId)) {
                        throw new AccessDeniedException(
                                        "Payment for order " + orderId + " does not belong to user " + userId);
                }

                return paymentMapper.toResponseDto(payment);
        }


        private void restoreStockForOrder(Order order) {
                for (OrderItem item : order.getOrderItems()) {
                        Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Product not found with id: " + item.getProduct().getId()));

                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);

                        log.info("Stock restored: productId={}, quantity=+{}, newStock={}",
                                        product.getId(), item.getQuantity(), product.getStockQuantity());
                }
        }
}
