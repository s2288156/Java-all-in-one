package org.all.auth.controller;

import jakarta.validation.Valid;
import org.all.auth.dto.AdminResetPasswordRequest;
import org.all.auth.dto.KeycloakUserResponse;
import org.all.auth.service.KeycloakUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/admin/users")
public class UserManagementController {

    private final KeycloakUserService keycloakUserService;

    public UserManagementController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @GetMapping
    public ResponseEntity<List<KeycloakUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "20") int max) {
        return ResponseEntity.ok(keycloakUserService.getAllUsers(first, max));
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeycloakUserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(keycloakUserService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> body) {
        keycloakUserService.updateUser(id, body.get("username"), body.get("email"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        keycloakUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable String id,
            @Valid @RequestBody AdminResetPasswordRequest request) {
        keycloakUserService.resetPassword(id, request.getNewPassword(), request.isTemporary());
        return ResponseEntity.ok().build();
    }
}
