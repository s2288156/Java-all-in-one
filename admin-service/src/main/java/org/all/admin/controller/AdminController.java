package org.all.admin.controller;

import jakarta.validation.Valid;
import org.all.admin.dto.UserRequest;
import org.all.admin.dto.UserResponse;
import org.all.admin.feign.UserServiceClient;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserServiceClient userServiceClient;

    public AdminController(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ApiResponse<PageResponse<UserResponse>> result = userServiceClient.getAllUsers(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        ApiResponse<UserResponse> result = userServiceClient.getUserById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest user) {
        ApiResponse<UserResponse> result = userServiceClient.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest user) {
        ApiResponse<UserResponse> result = userServiceClient.updateUser(id, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<Void> result = userServiceClient.deleteUser(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Admin Service is running");
    }
}
