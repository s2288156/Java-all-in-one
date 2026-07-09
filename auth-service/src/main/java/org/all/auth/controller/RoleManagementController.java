package org.all.auth.controller;

import jakarta.validation.Valid;
import org.all.auth.dto.AssignRoleRequest;
import org.all.auth.dto.RoleResponse;
import org.all.auth.service.KeycloakRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/admin")
public class RoleManagementController {

    private final KeycloakRoleService keycloakRoleService;

    public RoleManagementController(KeycloakRoleService keycloakRoleService) {
        this.keycloakRoleService = keycloakRoleService;
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        return ResponseEntity.ok(keycloakRoleService.getRoles());
    }

    @PostMapping("/roles")
    public ResponseEntity<Void> createRole(@RequestBody Map<String, String> body) {
        keycloakRoleService.createRole(body.get("name"), body.get("description"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{name}")
    public ResponseEntity<Void> deleteRole(@PathVariable String name) {
        keycloakRoleService.deleteRole(name);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}/roles")
    public ResponseEntity<List<RoleResponse>> getUserRoles(@PathVariable String id) {
        return ResponseEntity.ok(keycloakRoleService.getUserRoles(id));
    }

    @PostMapping("/users/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable String id,
            @Valid @RequestBody AssignRoleRequest request) {
        keycloakRoleService.assignRoles(id, request.getRoleNames());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}/roles")
    public ResponseEntity<Void> removeRoles(
            @PathVariable String id,
            @Valid @RequestBody AssignRoleRequest request) {
        keycloakRoleService.removeRoles(id, request.getRoleNames());
        return ResponseEntity.ok().build();
    }
}
