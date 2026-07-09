package org.all.auth.service.impl;

import org.all.auth.client.KeycloakClient;
import org.all.auth.client.KeycloakClient.RealmUser;
import org.all.auth.dto.KeycloakUserResponse;
import org.all.auth.service.KeycloakUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final KeycloakClient keycloakClient;

    public KeycloakUserServiceImpl(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    @Override
    public KeycloakUserResponse getUser(String userId) {
        RealmUser user = keycloakClient.getUser(userId);
        return toResponse(user);
    }

    @Override
    public List<KeycloakUserResponse> getAllUsers(int first, int max) {
        List<RealmUser> users = keycloakClient.getAllUsers(first, max);
        return users.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void updateUser(String userId, String username, String email) {
        Map<String, Object> fields = Map.of(
                "username", username,
                "email", email
        );
        keycloakClient.updateUser(userId, fields);
    }

    @Override
    public void deleteUser(String userId) {
        keycloakClient.deleteUser(userId);
    }

    @Override
    public void resetPassword(String userId, String newPassword, boolean temporary) {
        keycloakClient.resetPassword(userId, newPassword, temporary);
    }

    private KeycloakUserResponse toResponse(RealmUser user) {
        return KeycloakUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(Boolean.TRUE.equals(user.getEnabled()))
                .emailVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .build();
    }
}
