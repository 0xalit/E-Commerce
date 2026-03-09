package com.example.e_commerce.payment;

import com.example.e_commerce.config.StripeConfig;
import com.example.e_commerce.payment.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//This endpoint is PUBLIC (no JWT) but secured via Stripe's webhook signature verification.

@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentService paymentService;
    private final StripeConfig stripeConfig;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        // 1. Verify webhook signature
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // 2. Handle the event
        String eventType = event.getType();
        log.info("Received Stripe webhook event: type={}, id={}", eventType, event.getId());

        switch (eventType) {
            case "payment_intent.succeeded" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intent != null) {
                    paymentService.handlePaymentSuccess(intent.getId());
                }
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intent != null) {
                    paymentService.handlePaymentFailure(intent.getId());
                }
            }
            default -> log.info("Unhandled Stripe event type: {}", eventType);
        }

        // 3. Always return 200 to acknowledge receipt
        return ResponseEntity.ok("Received");
    }
}
