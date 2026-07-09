package org.all.user.service;

import org.all.common.model.PageResponse;
import org.all.user.dto.UserRequest;
import org.all.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse createInternalUser(String keycloakId, String email, String username);
    UserResponse getUserById(Long id);
    UserResponse getUserByKeycloakId(String keycloakId);
    PageResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    void deleteByKeycloakId(String keycloakId);
}
