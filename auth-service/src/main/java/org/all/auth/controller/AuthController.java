package org.all.auth.controller;

import jakarta.validation.Valid;
import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.KeycloakTokenResponse;
import org.all.auth.dto.LoginRequest;
import org.all.auth.dto.LoginResponse;
import org.all.auth.dto.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakClient keycloakClient;

    public AuthController(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return keycloakClient.getToken(request.getEmail(), request.getPassword())
                .map(response -> {
                    LoginResponse loginResponse = LoginResponse.builder()
                            .token(response.getAccess_token())
                            .tokenType(response.getToken_type())
                            .expiresIn(response.getExpires_in())
                            .build();
                    return ResponseEntity.ok(loginResponse);
                });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return keycloakClient.refreshToken(request.getRefreshToken())
                .map(response -> {
                    LoginResponse loginResponse = LoginResponse.builder()
                            .token(response.getAccess_token())
                            .tokenType(response.getToken_type())
                            .expiresIn(response.getExpires_in())
                            .build();
                    return ResponseEntity.ok(loginResponse);
                });
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}