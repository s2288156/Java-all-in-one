package org.all.admin.feign;

import org.all.admin.dto.UserRequest;
import org.all.admin.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    List<UserResponse> getAllUsers();

    @PostMapping("/api/users")
    UserResponse createUser(@RequestBody UserRequest user);

    @PutMapping("/api/users/{id}")
    UserResponse updateUser(@PathVariable("id") Long id, @RequestBody UserRequest user);

    @DeleteMapping("/api/users/{id}")
    void deleteUser(@PathVariable("id") Long id);
}
