package org.all.admin.feign;

import org.all.admin.dto.UserRequest;
import org.all.admin.dto.UserResponse;
import org.all.common.model.ApiResponse;
import org.all.common.model.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    ApiResponse<PageResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @PostMapping("/api/users")
    ApiResponse<UserResponse> createUser(@RequestBody UserRequest user);

    @PutMapping("/api/users/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest user);

    @DeleteMapping("/api/users/{id}")
    ApiResponse<Void> deleteUser(@PathVariable("id") Long id);
}
