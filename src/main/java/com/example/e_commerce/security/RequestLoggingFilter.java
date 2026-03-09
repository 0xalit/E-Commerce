package com.example.e_commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs every incoming HTTP request and its response:
 * → [METHOD] URI (from IP)
 * ← STATUS (Xms)
 */
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);
        long startTime = System.currentTimeMillis();

        log.info("→ {} {} (from {})", method, uri, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("← {} {} {} ({}ms)", status, method, uri, duration);
            } else if (status >= 400) {
                log.warn("← {} {} {} ({}ms)", status, method, uri, duration);
            } else {
                log.info("← {} {} {} ({}ms)", status, method, uri, duration);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
