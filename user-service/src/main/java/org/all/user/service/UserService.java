package org.all.user.service;

import org.all.user.dto.UserRequest;
import org.all.user.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);
    
    UserResponse getUserById(Long id);
    
    List<UserResponse> getAllUsers();
    
    UserResponse updateUser(Long id, UserRequest request);
    
    void deleteUser(Long id);
}