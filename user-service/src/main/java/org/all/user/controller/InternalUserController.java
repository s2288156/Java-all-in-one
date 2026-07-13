package org.all.user.controller;

import jakarta.validation.Valid;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.all.user.dto.UserRequest;
import org.all.user.dto.UserResponse;
import org.all.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/internal")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createInternalUser(@RequestBody Map<String, String> body) {
        UserResponse user = userService.createInternalUser(
                body.get("keycloakId"),
                body.get("email"),
                body.get("username"),
                body.get("phone")
        );
        return ResponseEntity.status(201).body(ApiResponse.ok(user));
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<ApiResponse<UserResponse>> getByKeycloakId(@PathVariable String keycloakId) {
        UserResponse user = userService.getUserByKeycloakId(keycloakId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @DeleteMapping("/keycloak/{keycloakId}")
    public ResponseEntity<ApiResponse<Void>> deleteByKeycloakId(@PathVariable String keycloakId) {
        userService.deleteByKeycloakId(keycloakId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserResponse> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
