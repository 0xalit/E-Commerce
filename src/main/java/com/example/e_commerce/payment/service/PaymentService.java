package com.example.e_commerce.payment.service;

import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.payment.dto.PaymentResponseDto;

public interface PaymentService {

    /**
     * Creates an order and a Stripe PaymentIntent.
     * Returns payment details including the clientSecret needed to confirm payment.
     */
    PaymentResponseDto createPaymentIntent(Long userId, OrderRequestDto orderRequestDto);

    /**
     * Called by the Stripe webhook when payment succeeds.
     * Confirms the order and clears the user's cart.
     */
    void handlePaymentSuccess(String paymentIntentId);

    /**
     * Called by the Stripe webhook when payment fails.
     * Restores stock and marks the order as PAYMENT_FAILED.
     */
    void handlePaymentFailure(String paymentIntentId);

    /**
     * Get payment details for a specific order (authenticated user).
     */
    PaymentResponseDto getPaymentByOrderId(Long userId, Long orderId);
}
