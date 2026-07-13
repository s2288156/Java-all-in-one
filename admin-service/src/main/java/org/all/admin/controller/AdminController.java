package org.all.admin.controller;

import jakarta.validation.Valid;
import org.all.admin.dto.*;
import org.all.admin.feign.AuthServiceClient;
import org.all.admin.feign.UserServiceClient;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AuthServiceClient authServiceClient;
    private final UserServiceClient userServiceClient;

    public AdminController(AuthServiceClient authServiceClient, UserServiceClient userServiceClient) {
        this.authServiceClient = authServiceClient;
        this.userServiceClient = userServiceClient;
    }

    // ==================== 用户管理 ====================

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
    public ResponseEntity<ApiResponse<Void>> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(request.getUsername());
        registerRequest.setPassword(request.getPassword());
        registerRequest.setEmail(request.getEmail());
        registerRequest.setPhone(request.getPhone());
        authServiceClient.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            try {
                Map<String, String> keycloakBody = new HashMap<>();
                keycloakBody.put("username", request.getUsername());
                if (request.getEmail() != null) {
                    keycloakBody.put("email", request.getEmail());
                }
                authServiceClient.updateUser(userResult.getData().getKeycloakId(), keycloakBody);
            } catch (Exception e) {
                log.warn("Failed to update Keycloak user: {}", e.getMessage());
            }
        }
        userServiceClient.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            try {
                authServiceClient.deleteUser(userResult.getData().getKeycloakId());
            } catch (Exception e) {
                log.warn("Failed to delete Keycloak user: {}", e.getMessage());
            }
        }
        userServiceClient.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ==================== 密码管理 ====================

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            authServiceClient.resetPassword(userResult.getData().getKeycloakId(), request);
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.ok(null));
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(authServiceClient.getRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<Void> createRole(@RequestBody Map<String, String> body) {
        authServiceClient.createRole(body);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{name}")
    public ResponseEntity<Void> deleteRole(@PathVariable String name) {
        authServiceClient.deleteRole(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}/roles")
    public ResponseEntity<List<RoleResponse>> getUserRoles(@PathVariable Long id) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            return ResponseEntity.ok(authServiceClient.getUserRoles(userResult.getData().getKeycloakId()));
        }
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            authServiceClient.assignRoles(userResult.getData().getKeycloakId(), request);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/users/{id}/roles")
    public ResponseEntity<Void> removeRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {
        ApiResponse<UserResponse> userResult = userServiceClient.getUserById(id);
        if (userResult.getData() != null && userResult.getData().getKeycloakId() != null) {
            authServiceClient.removeRoles(userResult.getData().getKeycloakId(), request);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // ==================== 健康检查 ====================

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Admin Service is running");
    }
}
