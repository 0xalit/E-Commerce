package com.example.e_commerce.payment;

import com.example.e_commerce.order.dto.OrderRequestDto;
import com.example.e_commerce.payment.dto.PaymentResponseDto;
import com.example.e_commerce.payment.service.PaymentService;
import com.example.e_commerce.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPaymentIntent(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(securityUtils.getCurrentUserId(), orderRequestDto));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(securityUtils.getCurrentUserId(), orderId));
    }
}
