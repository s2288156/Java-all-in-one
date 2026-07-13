package org.all.admin.feign;

import org.all.admin.dto.AdminResetPasswordRequest;
import org.all.admin.dto.AssignRoleRequest;
import org.all.admin.dto.KeycloakUserResponse;
import org.all.admin.dto.RegisterRequest;
import org.all.admin.dto.RoleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @PostMapping("/api/auth/register")
    Map<String, Object> register(@RequestBody RegisterRequest request);

    @GetMapping("/api/auth/admin/users")
    List<KeycloakUserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "20") int max);

    @GetMapping("/api/auth/admin/users/{id}")
    KeycloakUserResponse getUser(@PathVariable("id") String id);

    @PutMapping("/api/auth/admin/users/{id}")
    void updateUser(@PathVariable("id") String id, @RequestBody Map<String, String> body);

    @DeleteMapping("/api/auth/admin/users/{id}")
    void deleteUser(@PathVariable("id") String id);

    @PostMapping("/api/auth/admin/users/{id}/reset-password")
    void resetPassword(@PathVariable("id") String id, @RequestBody AdminResetPasswordRequest request);

    @GetMapping("/api/auth/admin/roles")
    List<RoleResponse> getRoles();

    @PostMapping("/api/auth/admin/roles")
    void createRole(@RequestBody Map<String, String> body);

    @DeleteMapping("/api/auth/admin/roles/{name}")
    void deleteRole(@PathVariable("name") String name);

    @GetMapping("/api/auth/admin/users/{id}/roles")
    List<RoleResponse> getUserRoles(@PathVariable("id") String id);

    @PostMapping("/api/auth/admin/users/{id}/roles")
    void assignRoles(@PathVariable("id") String id, @RequestBody AssignRoleRequest request);

    @DeleteMapping("/api/auth/admin/users/{id}/roles")
    void removeRoles(@PathVariable("id") String id, @RequestBody AssignRoleRequest request);
}
