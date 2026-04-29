package org.all.auth.controller;

import jakarta.validation.Valid;
import org.all.auth.config.JwtConfig;
import org.all.auth.dto.LoginRequest;
import org.all.auth.dto.LoginResponse;
import org.all.auth.dto.ValidateResponse;
import org.all.auth.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;

    public AuthController(JwtUtil jwtUtil, JwtConfig jwtConfig) {
        this.jwtUtil = jwtUtil;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        
        String token = jwtUtil.generateToken(request.getEmail(), claims);
        
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration() / 1000)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam("token") String token) {
        boolean isValid = jwtUtil.validateToken(token);
        String username = isValid ? jwtUtil.extractUsername(token) : null;
        
        ValidateResponse response = ValidateResponse.builder()
                .valid(isValid)
                .username(username)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }
}