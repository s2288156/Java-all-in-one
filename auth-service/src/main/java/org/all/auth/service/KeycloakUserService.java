package org.all.auth.service;

import org.all.auth.dto.KeycloakUserResponse;

import java.util.List;

public interface KeycloakUserService {
    KeycloakUserResponse getUser(String userId);
    List<KeycloakUserResponse> getAllUsers(int first, int max);
    void updateUser(String userId, String username, String email);
    void deleteUser(String userId);
    void resetPassword(String userId, String newPassword, boolean temporary);
}
