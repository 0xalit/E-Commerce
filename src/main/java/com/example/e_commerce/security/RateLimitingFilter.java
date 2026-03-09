package com.example.e_commerce.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using the Token Bucket algorithm (Bucket4j).
 *
 * Limits:
 * - /api/auth/** → 5 requests per minute (brute-force protection)
 * - All other endpoints → 60 requests per minute
 *
 * Buckets are stored per client IP in a ConcurrentHashMap (in-memory).
 * Upgrade to Redis-backed storage when scaling to multiple instances.
 */
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // Separate maps for the two limit tiers
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    // --- Bucket factories ---

    private Bucket createAuthBucket() {
        // 5 requests per minute for auth endpoints
        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket() {
        // 60 requests per minute for all other endpoints
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillGreedy(60, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    // --- Filter logic ---

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String uri = request.getRequestURI();

        boolean isAuthEndpoint = uri.startsWith("/api/auth/");

        Bucket bucket = isAuthEndpoint
                ? authBuckets.computeIfAbsent(clientIp, ip -> createAuthBucket())
                : generalBuckets.computeIfAbsent(clientIp, ip -> createGeneralBucket());

        long remainingTokens = bucket.getAvailableTokens();
        long capacity = isAuthEndpoint ? 5 : 60;

        if (bucket.tryConsume(1)) {
            // Inform the client about their remaining quota
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(capacity));
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens - 1));
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP '{}' on {} {}", clientIp, request.getMethod(), uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(capacity));
            response.addHeader("X-Rate-Limit-Remaining", "0");
            response.getWriter().write(
                    "{\"error\":\"too_many_requests\"," +
                            "\"message\":\"Rate limit exceeded. Please slow down and try again later.\"}");
        }
    }

    /**
     * Resolves the real client IP, respecting common proxy headers.
     */
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be a comma-separated list; first entry is the original
            // client
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
