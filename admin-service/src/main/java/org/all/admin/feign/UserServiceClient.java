package org.all.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    ResponseEntity<List<Map<String, Object>>> getAllUsers();

    @PostMapping("/api/users")
    ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> user);

    @PutMapping("/api/users/{id}")
    ResponseEntity<Map<String, Object>> updateUser(@PathVariable("id") Long id, @RequestBody Map<String, Object> user);

    @DeleteMapping("/api/users/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable("id") Long id);
}