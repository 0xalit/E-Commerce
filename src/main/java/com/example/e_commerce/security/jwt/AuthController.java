package com.example.e_commerce.security.jwt;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public static class LoginResponse {
        public String access_token;
        public String token_type;
        public long expires_in;
        public String expires_at;
        public List<String> roles;

        public LoginResponse(String token, long expiresInSeconds, List<String> roles) {
            this.access_token = token;
            this.token_type = "Bearer";
            this.expires_in = expiresInSeconds;
            this.expires_at = Instant.now().plusSeconds(expiresInSeconds).toString();
            this.roles = roles;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            String email = authentication.getName();
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtUtil.generateToken(email, roles);

            long expiresInSeconds = jwtUtil.getExpirationMillis() / 1000L;

            LoginResponse response = new LoginResponse(token, expiresInSeconds, roles);
            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }
}
