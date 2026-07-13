package org.all.admin.feign;

import org.all.admin.dto.UserRequest;
import org.all.admin.dto.UserResponse;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/internal")
    ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/users/internal/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/internal/keycloak/{keycloakId}")
    ApiResponse<UserResponse> getByKeycloakId(@PathVariable("keycloakId") String keycloakId);

    @PutMapping("/api/users/internal/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest user);

    @DeleteMapping("/api/users/internal/{id}")
    ApiResponse<Void> deleteUser(@PathVariable("id") Long id);

    @DeleteMapping("/api/users/internal/keycloak/{keycloakId}")
    ApiResponse<Void> deleteByKeycloakId(@PathVariable("keycloakId") String keycloakId);
}
