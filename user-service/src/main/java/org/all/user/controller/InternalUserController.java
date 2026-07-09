package org.all.user.controller;

import org.all.common.model.ApiResponse;
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
                body.get("username")
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
}
