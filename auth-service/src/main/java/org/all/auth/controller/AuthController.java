package org.all.auth.controller;

import jakarta.validation.Valid;
import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.KeycloakTokenResponse;
import org.all.auth.dto.LoginRequest;
import org.all.auth.dto.LoginResponse;
import org.all.auth.dto.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakClient keycloakClient;

    public AuthController(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        KeycloakTokenResponse response = keycloakClient.getToken(request.getEmail(), request.getPassword());
        LoginResponse loginResponse = LoginResponse.builder()
                .token(response.getAccess_token())
                .tokenType(response.getToken_type())
                .expiresIn(response.getExpires_in())
                .build();
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        KeycloakTokenResponse response = keycloakClient.refreshToken(request.getRefreshToken());
        LoginResponse loginResponse = LoginResponse.builder()
                .token(response.getAccess_token())
                .tokenType(response.getToken_type())
                .expiresIn(response.getExpires_in())
                .build();
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}
