package com.example.e_commerce.security.jwt;

import com.example.e_commerce.security.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // parseClaims() throws directly — exceptions are no longer swallowed
                Claims claims = jwtUtil.parseClaims(token);
                String email = claims.getSubject();

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authenticated user '{}' via JWT", email);
                }

            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired for request {} {}", request.getMethod(), request.getRequestURI());
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "token_expired",
                        "Your session has expired. Please log in again.");
                return;
            } catch (JwtException e) {
                log.warn("Invalid JWT token for request {} {}: {}", request.getMethod(), request.getRequestURI(),
                        e.getMessage());
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token",
                        "Invalid authentication token.");
                return;
            } catch (Exception e) {
                log.error("Unexpected auth error for request {} {}", request.getMethod(), request.getRequestURI(), e);
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "auth_error",
                        "Authentication failed unexpectedly.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                String.format("{\"error\":\"%s\",\"message\":\"%s\"}", error, message));
    }
}
