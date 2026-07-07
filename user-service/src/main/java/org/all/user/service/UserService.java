package org.all.user.service;

import org.all.common.model.PageResponse;
import org.all.user.dto.UserRequest;
import org.all.user.dto.UserResponse;

public interface UserService {

    UserResponse createUser(UserRequest request);
    
    UserResponse getUserById(Long id);
    
    PageResponse<UserResponse> getAllUsers(int page, int size);
    
    UserResponse updateUser(Long id, UserRequest request);
    
    void deleteUser(Long id);
}
