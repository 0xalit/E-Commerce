package com.example.e_commerce.payment.dto;

import com.example.e_commerce.payment.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private Long id;
    private Long orderId;
    private String stripePaymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}
